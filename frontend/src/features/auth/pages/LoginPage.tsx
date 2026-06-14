import { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '@/providers/AuthProvider';
import { APP } from '@/lib/constants';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';

export function LoginPage() {
  const { isAuthenticated, isLoading, login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      navigate('/customer/dashboard', { replace: true });
    }
  }, [isAuthenticated, isLoading, navigate]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/5 to-background">
        <LoadingSpinner size="lg" label="Chargement…" />
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/5 to-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-primary mb-4">
            <span className="text-xl font-bold text-primary-foreground">J</span>
          </div>
          <CardTitle className="text-2xl">Connexion</CardTitle>
          <CardDescription>
            Connectez-vous à votre espace {APP.fullName}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Button onClick={login} size="lg" className="w-full">
            Se connecter avec JIRAMA
          </Button>
          <p className="text-center text-xs text-muted-foreground">
            Vous serez redirigé vers la page de connexion sécurisée
          </p>
          <div className="border-t border-border pt-4 text-center">
            <p className="text-sm text-muted-foreground">
              Pas encore de compte ?{' '}
              <Link to="/register" className="text-primary font-medium hover:underline">
                Créez-en un
              </Link>
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
