import { Outlet } from 'react-router-dom';
import { Topbar } from './Topbar';
import { Footer } from './Footer';

/**
 * Public layout used for landing pages (no authentication required).
 */
export function PublicLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Topbar variant="public" />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
