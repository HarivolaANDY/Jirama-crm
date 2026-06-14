import { useState, useEffect } from 'react';
import { FileText, CreditCard, Zap, AlertTriangle, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '@/providers/AuthProvider';
import { PageHeader } from '@/components/shared/PageHeader';
import { StatCard } from '@/components/shared/StatCard';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import { EmptyState } from '@/components/shared/EmptyState';
import { apiClient } from '@/api/client';
import { formatCurrency } from '@/lib/formatters';

interface InvoiceSummary {
  id: string;
  invoiceNumber: string;
  periodLabel: string;
  totalAmount: number;
  status: string;
}

interface CurrentBill {
  id: string;
  invoiceNumber: string;
  totalAmount: number;
  balanceDue: number;
  dueDate: string;
  isOverdue: boolean;
}

interface ConsumptionSummary {
  currentKwh: number;
  previousKwh: number;
  changePercent: number;
}

interface DashboardData {
  currentBill: CurrentBill | null;
  recentInvoices: InvoiceSummary[];
  consumption: ConsumptionSummary;
  openIncidents: number;
}

export function DashboardPage() {
  const { user } = useAuth();
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiClient.get<DashboardData>('/dashboard/customer')
      .then(res => setData(res.data))
      .catch(() => {
        // Fallback to simulated data if API is unavailable
        setData(null);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <LoadingSpinner size="lg" label="Chargement du tableau de bord…" />
      </div>
    );
  }

  // We'll show the full dashboard even with fallback/empty data
  const bill = data?.currentBill;
  const recentInvoices = data?.recentInvoices ?? [];
  const consumption = data?.consumption;
  const openIncidents = data?.openIncidents ?? 0;

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Bonjour, ${user?.firstName ?? 'Client'}`}
        description="Voici un résumé de vos services JIRAMA"
      />

      {/* KPI Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Facture en cours"
          value={bill ? formatCurrency(bill.balanceDue) : '0 Ar'}
          icon={FileText}
          trend={bill?.isOverdue
            ? { value: 100, isPositive: false }
            : undefined
          }
        />
        <StatCard
          label="Consommation du mois"
          value={consumption ? `${Math.round(consumption.currentKwh)} kWh` : '—'}
          icon={Zap}
          trend={consumption && consumption.previousKwh > 0
            ? { value: Math.abs(Math.round(consumption.changePercent)), isPositive: consumption.changePercent < 0 }
            : undefined
          }
        />
        <StatCard
          label="Paiements effectués"
          value={`${recentInvoices.filter(i => i.status === 'PAID').length}`}
          icon={CreditCard}
        />
        <StatCard
          label="Incidents en cours"
          value={`${openIncidents}`}
          icon={AlertTriangle}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Recent Bills */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>Dernières factures</CardTitle>
            <Button variant="ghost" size="sm" asChild>
              <Link to="/customer/bills">
                Voir tout <ArrowRight className="ml-1 h-3 w-3" />
              </Link>
            </Button>
          </CardHeader>
          <CardContent>
            {recentInvoices.length === 0 ? (
              <EmptyState
                title="Aucune facture"
                description="Vos factures apparaîtront ici une fois générées."
              />
            ) : (
              <div className="space-y-3">
                {recentInvoices.map((inv) => (
                  <div
                    key={inv.id}
                    className="flex items-center justify-between rounded-lg border border-border p-3 transition-colors hover:bg-secondary/50"
                  >
                    <div>
                      <p className="text-sm font-medium text-foreground">{inv.periodLabel}</p>
                      <p className="text-xs text-muted-foreground">{inv.invoiceNumber}</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="text-sm font-semibold text-foreground">
                        {formatCurrency(inv.totalAmount)}
                      </span>
                      <StatusBadge status={inv.status} />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Quick Actions & Incidents */}
        <Card>
          <CardHeader>
            <CardTitle>Actions rapides</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button className="w-full justify-start" variant="outline" asChild>
              <Link to="/customer/bills">
                <FileText className="mr-2 h-4 w-4" />
                Payer ma facture
              </Link>
            </Button>
            <Button className="w-full justify-start" variant="outline" asChild>
              <Link to="/customer/consumption">
                <Zap className="mr-2 h-4 w-4" />
                Voir ma consommation
              </Link>
            </Button>
            <Button className="w-full justify-start" variant="outline" asChild>
              <Link to="/customer/incidents">
                <AlertTriangle className="mr-2 h-4 w-4" />
                Signaler un incident
              </Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
