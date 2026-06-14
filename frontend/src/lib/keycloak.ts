import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8081',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'jirama',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'jirama-frontend',
};

let keycloak: Keycloak | null = null;

export function getKeycloakInstance(): Keycloak {
  if (!keycloak) {
    keycloak = new Keycloak(keycloakConfig);
  }
  return keycloak;
}

export interface KeycloakUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  isAuthenticated: boolean;
}

/**
 * Extract user info and roles from the Keycloak token.
 */
export function extractUserInfo(kc: Keycloak): KeycloakUser {
  const tokenParsed = kc.tokenParsed;
  const realmRoles = (tokenParsed?.realm_access?.roles as string[]) ?? [];

  return {
    id: kc.subject ?? '',
    email: tokenParsed?.email as string ?? '',
    firstName: tokenParsed?.given_name as string ?? '',
    lastName: tokenParsed?.family_name as string ?? '',
    roles: realmRoles,
    isAuthenticated: kc.authenticated ?? false,
  };
}

/**
 * Initialize Keycloak with PKCE flow.
 * Silent SSO check uses an iframe for seamless re-authentication.
 */
export async function initKeycloak(): Promise<Keycloak> {
  const kc = getKeycloakInstance();

  try {
    const authenticated = await kc.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri:
        window.location.origin + '/silent-check-sso.html',
      pkceMethod: 'S256',
      checkLoginIframe: true,
      checkLoginIframeInterval: 30,
    });

    // Auto-refresh token 30 seconds before expiry
    if (authenticated) {
      setInterval(() => {
        kc.updateToken(30).catch(() => {
          console.warn('Keycloak token refresh failed');
        });
      }, 30_000);
    }

    return kc;
  } catch (error) {
    console.error('Keycloak initialization failed:', error);
    throw error;
  }
}
