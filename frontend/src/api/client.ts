import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { getKeycloakInstance } from '@/lib/keycloak';
import { API_PREFIX } from '@/lib/constants';
import type { ApiError } from '@/types/api';

/**
 * Axios instance configured for JIRAMA API.
 * Automatically attaches the Keycloak JWT to all requests.
 */
export const apiClient = axios.create({
  baseURL: API_PREFIX,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30_000,
});

/**
 * Request interceptor — attaches JWT Bearer token.
 */
apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const kc = getKeycloakInstance();

    if (kc.authenticated) {
      try {
        // Refresh token if it expires within 30 seconds
        await kc.updateToken(30);
        config.headers.Authorization = `Bearer ${kc.token}`;
      } catch {
        // Token refresh failed — redirect to login
        kc.login();
        return Promise.reject(new Error('Token refresh failed'));
      }
    }

    return config;
  },
  (error) => Promise.reject(error),
);

/**
 * Response interceptor — handles common errors.
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          // Unauthorized — redirect to login
          getKeycloakInstance().login();
          break;
        case 403:
          console.warn('Access denied:', data?.message);
          break;
        case 409:
          // Conflict — business rule violation
          console.warn('Business rule violation:', data?.message);
          break;
        case 422:
          console.warn('Validation error:', data?.errors);
          break;
        case 429:
          console.warn('Rate limited');
          break;
      }
    } else if (error.request) {
      console.error('Network error — no response received');
    }

    return Promise.reject(error);
  },
);

/**
 * Helper to extract a user-friendly error message from an API error.
 */
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data;
    if (apiError?.message) return apiError.message;
    if (error.response?.status === 401) return 'Session expirée. Veuillez vous reconnecter.';
    if (error.response?.status === 403) return 'Accès refusé.';
    if (error.response?.status === 429) return 'Trop de requêtes. Veuillez réessayer plus tard.';
    if (error.code === 'ERR_NETWORK') return 'Erreur réseau. Vérifiez votre connexion.';
    return 'Une erreur est survenue. Veuillez réessayer.';
  }
  if (error instanceof Error) return error.message;
  return 'Erreur inconnue.';
}

/**
 * Helper to extract validation errors from an API error.
 */
export function getValidationErrors(error: unknown): Record<string, string> | null {
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data;
    if (apiError?.errors && apiError.errors.length > 0) {
      return apiError.errors.reduce<Record<string, string>>(
        (acc, err) => {
          acc[err.field] = err.message;
          return acc;
        },
        {},
      );
    }
  }
  return null;
}
