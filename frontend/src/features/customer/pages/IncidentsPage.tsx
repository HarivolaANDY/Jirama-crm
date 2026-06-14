import { useState, useEffect } from 'react';
import { Plus, AlertTriangle, Clock, MapPin, MessageSquare, Droplets } from 'lucide-react';
import { PageHeader } from '@/components/shared/PageHeader';
import { Button } from '@/components/ui/button';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { EmptyState } from '@/components/shared/EmptyState';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { apiClient } from '@/api/client';
import { formatDate, formatRelativeDate } from '@/lib/formatters';
import { ReportIncidentDialog } from '@/features/customer/components/ReportIncidentDialog';

interface IncidentRow {
  id: string;
  incidentNumber: string;
  incidentType: string;
  severity: string;
  status: string;
  description: string;
  address: string | null;
  createdAt: string;
  resolvedAt: string | null;
}

const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: 'bg-red-100 text-red-700 border-red-200 dark:bg-red-950 dark:text-red-300',
  HIGH: 'bg-orange-100 text-orange-700 border-orange-200 dark:bg-orange-950 dark:text-orange-300',
  MEDIUM: 'bg-yellow-100 text-yellow-700 border-yellow-200 dark:bg-yellow-950 dark:text-yellow-300',
  LOW: 'bg-green-100 text-green-700 border-green-200 dark:bg-green-950 dark:text-green-300',
};

function SeverityBadge({ severity }: { severity: string }) {
  return (
    <span className={`inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-medium ${SEVERITY_COLORS[severity] ?? ''}`}>
      {severity === 'CRITICAL' ? 'Critique'
        : severity === 'HIGH' ? 'Élevée'
        : severity === 'MEDIUM' ? 'Moyenne'
        : 'Faible'}
    </span>
  );
}

function IncidentTypeIcon({ type }: { type: string }) {
  if (type === 'POWER_OUTAGE' || type === 'VOLTAGE_FLUCTUATION' || type === 'LINE_BREAK' || type === 'TRANSFORMER_FAILURE')
    return <AlertTriangle className="h-5 w-5 text-energy" />;
  if (type === 'WATER_OUTAGE' || type === 'WATER_LEAK' || type === 'LOW_PRESSURE')
    return <Droplets className="h-5 w-5 text-primary" />;
  return <AlertTriangle className="h-5 w-5 text-muted-foreground" />;
}

function typeLabel(type: string): string {
  const labels: Record<string, string> = {
    POWER_OUTAGE: 'Coupure électricité',
    WATER_OUTAGE: 'Coupure eau',
    VOLTAGE_FLUCTUATION: 'Variation tension',
    METER_MALFUNCTION: 'Compteur défectueux',
    LINE_BREAK: 'Câble cassé',
    TRANSFORMER_FAILURE: 'Panne transformateur',
    WATER_LEAK: 'Fuite d\'eau',
    LOW_PRESSURE: 'Basse pression',
    OTHER: 'Autre',
  };
  return labels[type] ?? type;
}


export function IncidentsPage() {
  const [incidents, setIncidents] = useState<IncidentRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);

  const fetchIncidents = () => {
    setLoading(true);
    apiClient.get<IncidentRow[]>('/incidents/my')
      .then(res => setIncidents(res.data))
      .catch(() => setIncidents([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchIncidents();
  }, []);

  const openIncidents = incidents.filter(i => !['RESOLVED', 'CLOSED', 'CANCELLED'].includes(i.status));

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <LoadingSpinner size="lg" label="Chargement des incidents…" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Mes incidents"
        description={
          openIncidents.length > 0
            ? `${openIncidents.length} incident${openIncidents.length > 1 ? 's' : ''} en cours`
            : 'Signalez et suivez vos incidents'
        }
        actions={
          <Button onClick={() => setDialogOpen(true)}>
            <Plus className="mr-2 h-4 w-4" />
            Signaler un incident
          </Button>
        }
      />

      {/* Open incidents */}
      {openIncidents.length > 0 && (
        <div className="space-y-4">
          <h2 className="text-lg font-semibold text-foreground">Incidents en cours</h2>
          <div className="grid gap-4">
            {openIncidents.map((inc) => (
              <Card key={inc.id} className="border-l-4 border-l-energy">
                <CardContent className="p-4">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex gap-3">
                      <div className="mt-1">
                        <IncidentTypeIcon type={inc.incidentType} />
                      </div>
                      <div className="space-y-1">
                        <div className="flex items-center gap-2 flex-wrap">
                          <h3 className="font-semibold text-foreground">
                            {typeLabel(inc.incidentType)}
                          </h3>
                          <SeverityBadge severity={inc.severity} />
                          <StatusBadge status={inc.status} />
                        </div>
                        <p className="text-sm text-muted-foreground">{inc.description}</p>
                        <div className="flex items-center gap-3 text-xs text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <Clock className="h-3 w-3" />
                            {formatRelativeDate(inc.createdAt)}
                          </span>
                          {inc.address && (
                            <span className="flex items-center gap-1">
                              <MapPin className="h-3 w-3" />
                              {inc.address}
                            </span>
                          )}
                          <span className="font-mono">{inc.incidentNumber}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* All incidents */}
      <div className="space-y-4">
        <h2 className="text-lg font-semibold text-foreground">
          {incidents.length > 0 ? 'Historique' : 'Mes incidents signalés'}
        </h2>

        {incidents.length === 0 ? (
          <EmptyState
            icon={AlertTriangle}
            title="Aucun incident"
            description="Vous n'avez signalé aucun incident. Utilisez le bouton ci-dessus pour en signaler un."
            action={
              <Button onClick={() => setDialogOpen(true)}>
                <Plus className="mr-2 h-4 w-4" />
                Signaler un incident
              </Button>
            }
          />
        ) : (
          <Card>
            <CardContent className="p-0">
              <div className="divide-y divide-border">
                {incidents.map((inc) => (
                  <div key={inc.id} className="flex items-start gap-4 p-4 transition-colors hover:bg-secondary/30">
                    <div className="mt-1">
                      <IncidentTypeIcon type={inc.incidentType} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap mb-1">
                        <h4 className="text-sm font-semibold text-foreground">
                          {typeLabel(inc.incidentType)}
                        </h4>
                        <SeverityBadge severity={inc.severity} />
                        <StatusBadge status={inc.status} />
                      </div>
                      <p className="text-sm text-muted-foreground line-clamp-2">{inc.description}</p>
                      <div className="flex items-center gap-3 mt-1 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {formatRelativeDate(inc.createdAt)}
                        </span>
                        <span className="font-mono">{inc.incidentNumber}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Report Incident Dialog */}
      <ReportIncidentDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        onSuccess={fetchIncidents}
      />
    </div>
  );
}
