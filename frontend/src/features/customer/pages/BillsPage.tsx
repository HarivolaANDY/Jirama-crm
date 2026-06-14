import { useState, useEffect } from 'react';
import { Download, CreditCard, Search } from 'lucide-react';
import { PageHeader } from '@/components/shared/PageHeader';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { EmptyState } from '@/components/shared/EmptyState';
import { Input } from '@/components/ui/input';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import { apiClient } from '@/api/client';
import { formatCurrency, formatDate } from '@/lib/formatters';

interface InvoiceRow {
  id: string;
  invoiceNumber: string;
  billingPeriodStart: string;
  billingPeriodEnd: string;
  issueDate: string;
  dueDate: string;
  status: string;
  totalAmount: number;
  amountPaid: number;
  balanceDue: number;
  consumptionKwh: number | null;
  pdfPath: string | null;
}

type StatusFilter = 'ALL' | 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED';

export function BillsPage() {
  const [invoices, setInvoices] = useState<InvoiceRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    apiClient.get<InvoiceRow[]>('/invoices/my')
      .then(res => setInvoices(res.data))
      .catch(() => setInvoices([]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = invoices.filter(inv => {
    if (statusFilter !== 'ALL' && inv.status !== statusFilter) return false;
    if (searchQuery && !inv.invoiceNumber.toLowerCase().includes(searchQuery.toLowerCase())) return false;
    return true;
  });

  // Sort by billing period descending
  filtered.sort((a, b) => new Date(b.billingPeriodEnd).getTime() - new Date(a.billingPeriodEnd).getTime());

  const statusFilters: { key: StatusFilter; label: string }[] = [
    { key: 'ALL', label: 'Toutes' },
    { key: 'PENDING', label: 'En attente' },
    { key: 'PAID', label: 'Payées' },
    { key: 'OVERDUE', label: 'En retard' },
  ];

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <LoadingSpinner size="lg" label="Chargement des factures…" />
      </div>
    );
  }

  const totalPending = invoices
    .filter(i => i.status === 'PENDING' || i.status === 'OVERDUE')
    .reduce((sum, i) => sum + i.balanceDue, 0);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Mes factures"
        description={
          totalPending > 0
            ? `Total dû : ${formatCurrency(totalPending)}`
            : 'Consultez et payez vos factures JIRAMA'
        }
        actions={
          invoices.length > 0 && (
            <Button variant="outline" disabled>
              <Download className="mr-2 h-4 w-4" />
              Tout télécharger
            </Button>
          )
        }
      />

      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            {/* Status filters */}
            <div className="flex gap-2">
              {statusFilters.map(f => (
                <button
                  key={f.key}
                  onClick={() => setStatusFilter(f.key)}
                  className={`rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${
                    statusFilter === f.key
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-secondary text-muted-foreground hover:bg-secondary/80'
                  }`}
                >
                  {f.label}
                </button>
              ))}
            </div>
            {/* Search */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Rechercher une facture…"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="pl-9 w-full sm:w-64"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-0">
          {filtered.length === 0 ? (
            <EmptyState
              title="Aucune facture"
              description={
                statusFilter !== 'ALL'
                  ? 'Aucune facture ne correspond à ce filtre.'
                  : "Vous n'avez pas encore de factures. Elles apparaîtront ici une fois générées."
              }
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border bg-secondary/50">
                    <th className="px-6 py-3 text-left font-medium text-muted-foreground">Période</th>
                    <th className="px-6 py-3 text-left font-medium text-muted-foreground">N° Facture</th>
                    <th className="px-6 py-3 text-right font-medium text-muted-foreground">Montant</th>
                    <th className="px-6 py-3 text-right font-medium text-muted-foreground">Dû</th>
                    <th className="px-6 py-3 text-center font-medium text-muted-foreground">Échéance</th>
                    <th className="px-6 py-3 text-center font-medium text-muted-foreground">Statut</th>
                    <th className="px-6 py-3 text-right font-medium text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {filtered.map((inv) => (
                    <tr key={inv.id} className="group transition-colors hover:bg-secondary/30">
                      <td className="px-6 py-4">
                        <span className="font-medium text-foreground">
                          {formatDate(inv.billingPeriodStart, 'month')}
                        </span>
                        <span className="text-muted-foreground"> — </span>
                        <span className="text-muted-foreground">
                          {formatDate(inv.billingPeriodEnd, 'day')}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-muted-foreground font-mono text-xs">
                        {inv.invoiceNumber}
                      </td>
                      <td className="px-6 py-4 text-right font-medium text-foreground">
                        {formatCurrency(inv.totalAmount)}
                      </td>
                      <td className="px-6 py-4 text-right">
                        {inv.balanceDue > 0 ? (
                          <span className="font-semibold text-destructive">
                            {formatCurrency(inv.balanceDue)}
                          </span>
                        ) : (
                          <span className="text-muted-foreground">—</span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-center text-muted-foreground">
                        {formatDate(inv.dueDate, 'day')}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <StatusBadge status={inv.status} />
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                          {(inv.status === 'PENDING' || inv.status === 'OVERDUE') && (
                            <Button size="sm" className="h-8">
                              <CreditCard className="mr-1 h-3 w-3" />
                              Payer
                            </Button>
                          )}
                          <Button size="sm" variant="ghost" className="h-8" disabled>
                            <Download className="h-3 w-3" />
                          </Button>
                        </div>
                        <span className="text-xs text-muted-foreground group-hover:hidden">
                          {inv.consumptionKwh != null ? `${Math.round(inv.consumptionKwh)} kWh` : '—'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Summary footer */}
      {invoices.length > 0 && (
        <Card>
          <CardContent className="flex items-center justify-between p-4">
            <span className="text-sm text-muted-foreground">
              {invoices.length} facture{invoices.length > 1 ? 's' : ''} au total
            </span>
            <span className="text-sm font-semibold text-foreground">
              Total impayé : {formatCurrency(totalPending)}
            </span>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
