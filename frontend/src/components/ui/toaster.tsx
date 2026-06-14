import { ToastProvider, ToastViewport } from './toast';

/**
 * Toaster component — renders the toast container.
 * Add this once at the root of the app (placed in App.tsx).
 */
export function Toaster() {
  return (
    <ToastProvider duration={5000}>
      <ToastViewport />
    </ToastProvider>
  );
}
