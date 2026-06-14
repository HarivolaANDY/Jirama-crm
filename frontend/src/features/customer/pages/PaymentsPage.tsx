import { PageHeader } from '@/components/shared/PageHeader';
import { EmptyState } from '@/components/shared/EmptyState';
import { CreditCard } from 'lucide-react';

export function PaymentsPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Historique des paiements"
        description="Consultez tous vos paiements effectués"
      />

      <EmptyState
        icon={CreditCard}
        title="Aucun paiement"
        description="Vos paiements apparaîtront ici une fois effectués."
      />
    </div>
  );
}
