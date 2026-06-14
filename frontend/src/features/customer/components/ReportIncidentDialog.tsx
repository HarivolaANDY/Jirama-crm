import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { apiClient } from '@/api/client';
import { getErrorMessage } from '@/api/client';
import { AlertTriangle, CheckCircle2 } from 'lucide-react';

const INCIDENT_TYPES = [
  { value: 'POWER_OUTAGE', label: 'Coupure d\'électricité' },
  { value: 'WATER_OUTAGE', label: 'Coupure d\'eau' },
  { value: 'VOLTAGE_FLUCTUATION', label: 'Variation de tension' },
  { value: 'METER_MALFUNCTION', label: 'Compteur défectueux' },
  { value: 'LINE_BREAK', label: 'Câble cassé' },
  { value: 'TRANSFORMER_FAILURE', label: 'Panne de transformateur' },
  { value: 'WATER_LEAK', label: 'Fuite d\'eau' },
  { value: 'LOW_PRESSURE', label: 'Basse pression' },
  { value: 'OTHER', label: 'Autre' },
];

const SEVERITIES = [
  { value: 'LOW', label: 'Faible' },
  { value: 'MEDIUM', label: 'Moyenne' },
  { value: 'HIGH', label: 'Élevée' },
  { value: 'CRITICAL', label: 'Critique' },
];

interface ReportIncidentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export function ReportIncidentDialog({ open, onOpenChange, onSuccess }: ReportIncidentDialogProps) {
  const [incidentType, setIncidentType] = useState('');
  const [severity, setSeverity] = useState('MEDIUM');
  const [description, setDescription] = useState('');
  const [address, setAddress] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!incidentType || !description.trim()) return;

    setSubmitting(true);
    setError(null);

    try {
      await apiClient.post('/incidents', {
        incidentType,
        severity,
        description: description.trim(),
        address: address.trim() || undefined,
      });
      setSuccess(true);
      setTimeout(() => {
        resetForm();
        onOpenChange(false);
        onSuccess();
      }, 1500);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const resetForm = () => {
    setIncidentType('');
    setSeverity('MEDIUM');
    setDescription('');
    setAddress('');
    setError(null);
    setSuccess(false);
  };

  return (          <Dialog open={open} onOpenChange={(o: boolean) => { if (!o) resetForm(); onOpenChange(o); }}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Signaler un incident</DialogTitle>
          <DialogDescription>
            Décrivez le problème que vous rencontrez pour que nous puissions intervenir.
          </DialogDescription>
        </DialogHeader>

        {success ? (
          <div className="flex flex-col items-center justify-center py-8">
            <CheckCircle2 className="h-12 w-12 text-success mb-4" />
            <p className="text-lg font-semibold text-foreground">Incident signalé !</p>
            <p className="text-sm text-muted-foreground mt-1">
              Votre incident a été enregistré. Nous allons intervenir dans les plus brefs délais.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="rounded-lg bg-destructive/10 border border-destructive/20 p-3 text-sm text-destructive flex items-start gap-2">
                <AlertTriangle className="h-4 w-4 mt-0.5 shrink-0" />
                {error}
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="incidentType">Type d'incident *</Label>
              <select
                id="incidentType"
                value={incidentType}
                onChange={e => setIncidentType(e.target.value)}
                required
                className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <option value="">Sélectionnez…</option>
                {INCIDENT_TYPES.map(t => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="severity">Gravité</Label>
              <div className="flex gap-2">
                {SEVERITIES.map(s => (
                  <button
                    key={s.value}
                    type="button"
                    onClick={() => setSeverity(s.value)}
                    className={`flex-1 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                      severity === s.value
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-secondary text-muted-foreground hover:bg-secondary/80'
                    }`}
                  >
                    {s.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description *</Label>
              <Textarea
                id="description"
                placeholder="Décrivez le problème (ex: 'Plus d'électricité dans le quartier depuis 2h')"
                value={description}
                onChange={e => setDescription(e.target.value)}
                required
                rows={4}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="address">Adresse / Lieu</Label>
              <Input
                id="address"
                placeholder="Lot, quartier, ou point de repère"
                value={address}
                onChange={e => setAddress(e.target.value)}
              />
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => { resetForm(); onOpenChange(false); }}>
                Annuler
              </Button>
              <Button type="submit" disabled={submitting || !incidentType || !description.trim()}>
                {submitting ? 'Envoi en cours…' : 'Signaler'}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
}
