import { cn } from '@/lib/utils';

type StatusVariant = 'success' | 'warning' | 'error' | 'info' | 'neutral';

interface StatusBadgeProps {
  status: string;
  variant?: StatusVariant;
  label?: string;
  className?: string;
}

const variantStyles: Record<StatusVariant, string> = {
  success: 'bg-success/10 text-success border-success/20',
  warning: 'bg-energy-50 text-energy-700 border-energy-200 dark:bg-energy-950 dark:text-energy-300',
  error: 'bg-destructive/10 text-destructive border-destructive/20',
  info: 'bg-primary/10 text-primary border-primary/20',
  neutral: 'bg-muted text-muted-foreground border-border',
};

/**
 * Maps common status strings to a variant color.
 */
function getVariant(status: string): StatusVariant {
  const s = status.toLowerCase();
  if (['active', 'paid', 'completed', 'resolved', 'approved', 'success'].includes(s)) return 'success';
  if (['pending', 'partially_paid', 'in_progress', 'scheduled'].includes(s)) return 'warning';
  if (['overdue', 'failed', 'cancelled', 'rejected', 'suspended', 'blacklisted'].includes(s)) return 'error';
  if (['reported', 'confirmed', 'assigned'].includes(s)) return 'info';
  return 'neutral';
}

/**
 * Formats a status key into a human-readable label.
 */
function formatStatus(status: string): string {
  return status
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

export function StatusBadge({
  status,
  variant,
  label,
  className,
}: StatusBadgeProps) {
  const resolvedVariant = variant ?? getVariant(status);

  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium',
        variantStyles[resolvedVariant],
        className,
      )}
    >
      {label ?? formatStatus(status)}
    </span>
  );
}
