import { useAuth } from '@/providers/AuthProvider';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';

export function ProfilePage() {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      <PageHeader title="Mon profil" description="Gérez vos informations personnelles" />

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <Avatar className="h-16 w-16">
              <AvatarFallback className="bg-primary/10 text-primary text-lg">
                {user?.firstName?.[0]}{user?.lastName?.[0]}
              </AvatarFallback>
            </Avatar>
            <div>
              <CardTitle>{user?.firstName} {user?.lastName}</CardTitle>
              <p className="text-sm text-muted-foreground">{user?.email}</p>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <dl className="grid gap-4 sm:grid-cols-2">
            <div>
              <dt className="text-sm text-muted-foreground">Email</dt>
              <dd className="text-sm font-medium text-foreground">{user?.email}</dd>
            </div>
            <div>
              <dt className="text-sm text-muted-foreground">Rôle</dt>
              <dd className="text-sm font-medium text-foreground">
                {user?.roles?.join(', ') ?? 'Client'}
              </dd>
            </div>
          </dl>
        </CardContent>
      </Card>
    </div>
  );
}
