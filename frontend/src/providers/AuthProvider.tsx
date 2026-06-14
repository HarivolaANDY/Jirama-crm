import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import Keycloak from 'keycloak-js';
import {
  extractUserInfo,
  getKeycloakInstance,
  initKeycloak,
  type KeycloakUser,
} from '@/lib/keycloak';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';

interface AuthContextType {
  keycloak: Keycloak | null;
  user: KeycloakUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: () => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  getToken: () => string | undefined;
}

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [keycloak, setKeycloak] = useState<Keycloak | null>(null);
  const [user, setUser] = useState<KeycloakUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    initKeycloak()
      .then((kc) => {
        if (!mounted) return;
        setKeycloak(kc);
        if (kc.authenticated) {
          setUser(extractUserInfo(kc));
        }
      })
      .catch((err) => {
        if (!mounted) return;
        setError(err instanceof Error ? err.message : 'Authentication failed');
        console.error('Auth init failed:', err);
      })
      .finally(() => {
        if (mounted) setIsLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, []);

  const login = useCallback(() => {
    const kc = keycloak ?? getKeycloakInstance();
    kc.login({ redirectUri: window.location.origin + '/customer/dashboard' });
  }, [keycloak]);

  const logout = useCallback(() => {
    keycloak?.logout({ redirectUri: window.location.origin });
  }, [keycloak]);

  const hasRole = useCallback(
    (role: string) => {
      return keycloak?.hasRealmRole(role) ?? false;
    },
    [keycloak],
  );

  const hasAnyRole = useCallback(
    (roles: string[]) => {
      return roles.some((role) => keycloak?.hasRealmRole(role) ?? false);
    },
    [keycloak],
  );

  const getToken = useCallback(() => {
    return keycloak?.token;
  }, [keycloak]);

  const value = useMemo<AuthContextType>(
    () => ({
      keycloak,
      user,
      isAuthenticated: keycloak?.authenticated ?? false,
      isLoading,
      login,
      logout,
      hasRole,
      hasAnyRole,
      getToken,
    }),
    [keycloak, user, isLoading, login, logout, hasRole, hasAnyRole, getToken],
  );

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="text-center">
          <h2 className="text-xl font-semibold text-destructive">
            Erreur d'authentification
          </h2>
          <p className="mt-2 text-muted-foreground">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 rounded-md bg-primary px-4 py-2 text-sm text-primary-foreground hover:bg-primary/90"
          >
            Réessayer
          </button>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
