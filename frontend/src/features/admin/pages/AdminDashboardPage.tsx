import { Users, Shield, FileText, Activity } from 'lucide-react';
import { PageHeader } from '@/components/shared/PageHeader';
import { StatCard } from '@/components/shared/StatCard';

export function AdminDashboardPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Administration"
        description="Gérez les utilisateurs et la configuration du système"
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Utilisateurs" value="156" icon={Users} />
        <StatCard label="Rôles" value="6" icon={Shield} />
        <StatCard label="Logs d'audit" value="12 458" icon={FileText} />
        <StatCard label="Système" value="OK" icon={Activity} />
      </div>
    </div>
  );
}
