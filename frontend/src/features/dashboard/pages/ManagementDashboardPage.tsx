import { DollarSign, Users, TrendingUp, PieChart } from 'lucide-react';
import { PageHeader } from '@/components/shared/PageHeader';
import { StatCard } from '@/components/shared/StatCard';

export function ManagementDashboardPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Tableau de bord Direction"
        description="Indicateurs clés de performance JIRAMA"
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Revenus du mois" value="2.4 M Ar" icon={DollarSign} trend={{ value: 8, isPositive: true }} />
        <StatCard label="Abonnés actifs" value="450 000" icon={Users} trend={{ value: 3, isPositive: true }} />
        <StatCard label="Taux de collecte" value="87%" icon={TrendingUp} trend={{ value: 2, isPositive: true }} />
        <StatCard label="Incidents critiques" value="12" icon={PieChart} trend={{ value: 25, isPositive: false }} />
      </div>
    </div>
  );
}
