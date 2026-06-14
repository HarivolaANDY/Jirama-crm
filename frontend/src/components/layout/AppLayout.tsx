import { Outlet } from 'react-router-dom';
import { Topbar } from './Topbar';
import { Sidebar } from './Sidebar';
import { MobileNav } from './MobileNav';
import { useIsMobile } from '@/hooks/useMediaQuery';

/**
 * Main authenticated layout with sidebar navigation.
 * Responsive: sidebar on desktop, bottom nav on mobile.
 */
export function AppLayout() {
  const isMobile = useIsMobile();

  return (
    <div className="flex min-h-screen bg-background">
      {!isMobile && <Sidebar />}
      <div className="flex flex-1 flex-col lg:pl-64">
        <Topbar variant="app" />
        <main className="flex-1 p-4 pb-20 lg:pb-4 lg:p-6">
          <Outlet />
        </main>
      </div>
      {isMobile && <MobileNav />}
    </div>
  );
}
