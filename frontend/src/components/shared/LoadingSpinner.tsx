import { cn } from '@/lib/utils';
import { Loader2 } from 'lucide-react';

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  label?: string;
}

const sizeMap = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
  lg: 'h-10 w-10',
};

export function LoadingSpinner({ size = 'md', className, label }: LoadingSpinnerProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3" role="status">
      <Loader2
        className={cn('animate-spin text-primary', sizeMap[size], className)}
      />
      {label && (
        <p className="text-sm text-muted-foreground">{label}</p>
      )}
      <span className="sr-only">Chargement en cours…</span>
    </div>
  );
}
