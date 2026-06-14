/**
 * Date and time formatting utilities.
 */

export const DATE_FORMATS = {
  short: { day: 'numeric' as const, month: 'short' as const, year: 'numeric' as const },
  long: { day: 'numeric' as const, month: 'long' as const, year: 'numeric' as const },
  full: { day: 'numeric' as const, month: 'long' as const, year: 'numeric' as const, hour: '2-digit' as const, minute: '2-digit' as const },
  month: { month: 'long' as const, year: 'numeric' as const },
  day: { day: 'numeric' as const, month: 'short' as const },
  time: { hour: '2-digit' as const, minute: '2-digit' as const },
};

const LOCALE = 'fr-FR';

export function formatDate(date: Date | string, format: keyof typeof DATE_FORMATS = 'short'): string {
  return new Intl.DateTimeFormat(LOCALE, DATE_FORMATS[format]).format(new Date(date));
}

export function formatRelativeDate(date: Date | string): string {
  const now = new Date();
  const target = new Date(date);
  const diffMs = now.getTime() - target.getTime();
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffHours < 1) return 'Il y a quelques minutes';
  if (diffHours < 24) return `Il y a ${diffHours}h`;
  if (diffDays === 1) return 'Hier';
  if (diffDays < 7) return `Il y a ${diffDays} jours`;
  return formatDate(date);
}

export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat(LOCALE, {
    style: 'currency',
    currency: 'MGA',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(amount);
}

export function formatNumber(value: number, decimals = 0): string {
  return new Intl.NumberFormat(LOCALE, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value);
}

export function formatPhoneNumber(phone: string): string {
  // Format +261 34 12 345 67
  const cleaned = phone.replace(/\D/g, '');
  if (cleaned.length === 12 && cleaned.startsWith('261')) {
    return `+261 ${cleaned.slice(3, 5)} ${cleaned.slice(5, 7)} ${cleaned.slice(7, 10)} ${cleaned.slice(10, 12)}`;
  }
  return phone;
}
