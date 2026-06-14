import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '@/providers/AuthProvider';
import { authApi, type RegisterRequest } from '@/api/auth.api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import { APP, MADAGASCAR_REGIONS } from '@/lib/constants';
import { getErrorMessage } from '@/api/client';

export function RegisterPage() {
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState<RegisterRequest>({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '+261',
    addressLine1: '',
    addressLine2: '',
    city: '',
    district: '',
    regionCode: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = (field: keyof RegisterRequest, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      const result = await authApi.register(formData);
      navigate('/customer/dashboard', {
        state: { registrationComplete: true, subscriberNumber: result.subscriberNumber },
      });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  // If already registered, redirect to dashboard
  if (!authLoading && isAuthenticated && user) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background p-4">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center">
            <CardTitle className="text-xl">Bienvenue {user.firstName} !</CardTitle>
            <CardDescription>
              Votre compte est déjà lié. Accédez à votre tableau de bord.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate('/customer/dashboard')} className="w-full">
              Tableau de bord
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <LoadingSpinner size="lg" label="Vérification de votre session…" />
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/5 to-background p-4">
      <Card className="w-full max-w-2xl">
        <CardHeader className="text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-primary mb-4">
            <span className="text-xl font-bold text-primary-foreground">J</span>
          </div>
          <CardTitle className="text-2xl">Finalisez votre inscription</CardTitle>
          <CardDescription>
            Complétez vos informations pour créer votre compte {APP.name}
          </CardDescription>
        </CardHeader>

        <CardContent>
          {error && (
            <div className="mb-6 rounded-lg bg-destructive/10 border border-destructive/20 p-4 text-sm text-destructive">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Name fields */}
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground" htmlFor="firstName">
                  Prénom *
                </label>
                <Input
                  id="firstName"
                  placeholder="Jean"
                  value={formData.firstName}
                  onChange={(e) => handleChange('firstName', e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground" htmlFor="lastName">
                  Nom *
                </label>
                <Input
                  id="lastName"
                  placeholder="Rakoto"
                  value={formData.lastName}
                  onChange={(e) => handleChange('lastName', e.target.value)}
                  required
                />
              </div>
            </div>

            {/* Contact */}
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground" htmlFor="email">
                  Email *
                </label>
                <Input
                  id="email"
                  type="email"
                  placeholder="jean.rakoto@email.com"
                  value={formData.email}
                  onChange={(e) => handleChange('email', e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground" htmlFor="phone">
                  Téléphone *
                </label>
                <Input
                  id="phone"
                  placeholder="+261 34 12 345 67"
                  value={formData.phoneNumber}
                  onChange={(e) => handleChange('phoneNumber', e.target.value)}
                  required
                />
                <p className="text-xs text-muted-foreground">
                  Format: +261 XX XXX XX XX
                </p>
              </div>
            </div>

            {/* Address */}
            <div className="space-y-2">
              <label className="text-sm font-medium text-foreground" htmlFor="addressLine1">
                Adresse *
              </label>
              <Input
                id="addressLine1"
                placeholder="Lot IVK 123, Ambohimanarina"
                value={formData.addressLine1}
                onChange={(e) => handleChange('addressLine1', e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-foreground" htmlFor="addressLine2">
                Complément d'adresse
              </label>
              <Input
                id="addressLine2"
                placeholder="Escalier C, 2ème étage (optionnel)"
                value={formData.addressLine2}
                onChange={(e) => handleChange('addressLine2', e.target.value)}
              />
            </div>

            {/* City / District / Region */}
            <div className="grid gap-4 sm:grid-cols-3">
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground" htmlFor="city">
                  Ville *
                </label>
                <Input
                  id="city"
                  placeholder="Antananarivo"
                  value={formData.city}
                  onChange={(e) => handleChange('city', e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground" htmlFor="district">
                  District
                </label>
                <Input
                  id="district"
                  placeholder="Ambohimanarina"
                  value={formData.district}
                  onChange={(e) => handleChange('district', e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground" htmlFor="regionCode">
                  Région
                </label>
                <select
                  id="regionCode"
                  value={formData.regionCode}
                  onChange={(e) => handleChange('regionCode', e.target.value)}
                  className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                >
                  <option value="">Sélectionnez…</option>
                  {MADAGASCAR_REGIONS.map((r) => (
                    <option key={r.code} value={r.code}>
                      {r.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <Button type="submit" className="w-full" size="lg" disabled={isSubmitting}>
              {isSubmitting ? 'Inscription en cours…' : 'Créer mon compte'}
            </Button>

            <p className="text-center text-xs text-muted-foreground">
              Déjà inscrit ?{' '}
              <Link to="/login" className="text-primary hover:underline">
                Connectez-vous
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
