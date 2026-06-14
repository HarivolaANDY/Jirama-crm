import { apiClient } from './client';

export interface KeycloakConfig {
  url: string;
  realm: string;
  clientId: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  district?: string;
  regionCode?: string;
}

export interface RegisterResult {
  id: string;
  subscriberNumber: string;
  fullName: string;
}

export const authApi = {
  /** Get Keycloak configuration for the frontend */
  getKeycloakConfig: async (): Promise<KeycloakConfig> => {
    const response = await apiClient.get('/auth/keycloak-config');
    return response.data;
  },

  /** Complete registration — links Keycloak user to a subscriber record */
  register: async (data: RegisterRequest): Promise<RegisterResult> => {
    const response = await apiClient.post('/auth/register', data);
    return response.data;
  },

  /** Get current user info from the JWT */
  getMe: async () => {
    const response = await apiClient.get('/auth/me');
    return response.data;
  },
};
