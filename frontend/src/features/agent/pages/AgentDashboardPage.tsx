import { Users, FileText, AlertTriangle, CheckCircle } from 'lucide-react';
import { PageHeader } from '@/components/shared/PageHeader';
import { StatCard } from '@/components/shared/StatCard';

export function AgentDashboardPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Espace Agent"
        description="Gérez les abonnés, contrats, et interventions"
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Abonnés" value="12 450" icon={Users} trend={{ value: 5, isPositive: true }} />
        <StatCard label="Factures en attente" value="342" icon={FileText} trend={{ value: 8, isPositive: false }} />
        <StatCard label="Incidents ouverts" value="28" icon={AlertTriangle} trend={{ value: 15, isPositive: false }} />
        <StatCard label="Validations" value="15" icon={CheckCircle} trend={{ value: 10, isPositive: true }} />
      </div>
    </div>
  );
}
