import { ClipboardList, Map, Wrench, Timer } from 'lucide-react';
import { PageHeader } from '@/components/shared/PageHeader';
import { StatCard } from '@/components/shared/StatCard';

export function TechDashboardPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Espace Technicien"
        description="Gérez vos tournées et interventions"
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Tournées du jour" value="3" icon={ClipboardList} />
        <StatCard label="Compteurs à relever" value="45" icon={Map} />
        <StatCard label="Interventions" value="8" icon={Wrench} />
        <StatCard label="En cours" value="2" icon={Timer} />
      </div>
    </div>
  );
}
