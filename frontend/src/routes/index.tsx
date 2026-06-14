import { createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { PublicLayout } from '@/components/layout/PublicLayout';
import { AppLayout } from '@/components/layout/AppLayout';

// Lazy-loaded pages
import { HomePage } from '@/features/landing/pages/HomePage';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { RegisterPage } from '@/features/auth/pages/RegisterPage';

// Customer pages
import { DashboardPage as CustomerDashboard } from '@/features/customer/pages/DashboardPage';
import { BillsPage } from '@/features/customer/pages/BillsPage';
import { PaymentsPage } from '@/features/customer/pages/PaymentsPage';
import { ConsumptionPage } from '@/features/customer/pages/ConsumptionPage';
import { IncidentsPage } from '@/features/customer/pages/IncidentsPage';
import { ProfilePage } from '@/features/customer/pages/ProfilePage';

// Agent pages
import { AgentDashboardPage } from '@/features/agent/pages/AgentDashboardPage';

// Technician pages
import { TechDashboardPage } from '@/features/technician/pages/TechDashboardPage';

// Admin pages
import { AdminDashboardPage } from '@/features/admin/pages/AdminDashboardPage';

// Management pages
import { ManagementDashboardPage } from '@/features/dashboard/pages/ManagementDashboardPage';

export const router = createBrowserRouter([
  // ── Public Routes ──
  {
    element: <PublicLayout />,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },

  // ── Customer Portal ──
  {
    path: '/customer',
    element: <ProtectedRoute roles={['CUSTOMER']} />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: 'dashboard', element: <CustomerDashboard /> },
          { path: 'bills', element: <BillsPage /> },
          { path: 'payments', element: <PaymentsPage /> },
          { path: 'consumption', element: <ConsumptionPage /> },
          { path: 'incidents', element: <IncidentsPage /> },
          { path: 'profile', element: <ProfilePage /> },
        ],
      },
    ],
  },

  // ── Agent CRM ──
  {
    path: '/agent',
    element: <ProtectedRoute roles={['AGENT', 'ADMIN']} />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: 'dashboard', element: <AgentDashboardPage /> },
          { path: 'subscribers', element: <div>Subscribers</div> },
          { path: 'contracts', element: <div>Contracts</div> },
          { path: 'meters', element: <div>Meters</div> },
          { path: 'billing', element: <div>Billing</div> },
          { path: 'incidents', element: <div>Incidents</div> },
          { path: 'complaints', element: <div>Complaints</div> },
        ],
      },
    ],
  },

  // ── Technician CRM ──
  {
    path: '/technician',
    element: <ProtectedRoute roles={['TECHNICIAN']} />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: 'dashboard', element: <TechDashboardPage /> },
          { path: 'routes', element: <div>My Routes</div> },
          { path: 'outages', element: <div>Outages</div> },
          { path: 'work-orders', element: <div>Work Orders</div> },
        ],
      },
    ],
  },

  // ── Admin Panel ──
  {
    path: '/admin',
    element: <ProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']} />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: 'dashboard', element: <AdminDashboardPage /> },
          { path: 'users', element: <div>Users</div> },
          { path: 'audit-logs', element: <div>Audit Logs</div> },
          { path: 'configuration', element: <div>Configuration</div> },
        ],
      },
    ],
  },

  // ── Management Dashboard ──
  {
    path: '/management',
    element: <ProtectedRoute roles={['MANAGER', 'ADMIN']} />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: 'dashboard', element: <ManagementDashboardPage /> },
        ],
      },
    ],
  },

  // ── Unauthorized ──
  {
    path: '/unauthorized',
    element: (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-destructive">403</h1>
          <p className="mt-2 text-muted-foreground">
            Accès refusé. Vous n'avez pas les permissions nécessaires.
          </p>
          <a
            href="/"
            className="mt-4 inline-block rounded-md bg-primary px-4 py-2 text-sm text-primary-foreground"
          >
            Retour à l'accueil
          </a>
        </div>
      </div>
    ),
  },

  // ── 404 ──
  {
    path: '*',
    element: (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-muted-foreground">404</h1>
          <p className="mt-2 text-muted-foreground">Page non trouvée.</p>
          <a
            href="/"
            className="mt-4 inline-block rounded-md bg-primary px-4 py-2 text-sm text-primary-foreground"
          >
            Retour à l'accueil
          </a>
        </div>
      </div>
    ),
  },
]);
