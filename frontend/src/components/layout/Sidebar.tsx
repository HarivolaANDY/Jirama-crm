import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  FileText,
  CreditCard,
  Zap,
  AlertTriangle,
  MessageSquare,
  User,
  Users,
  Gauge,
  ClipboardList,
  Settings,
  LogOut,
  Building2,
  type LucideIcon,
} from 'lucide-react';
import { useAuth } from '@/providers/AuthProvider';
import { cn } from '@/lib/utils';

interface NavItem {
  label: string;
  href: string;
  icon: LucideIcon;
  roles: string[];
}

const navItems: NavItem[] = [
  // Customer items
  { label: 'Tableau de bord', href: '/customer/dashboard', icon: LayoutDashboard, roles: ['CUSTOMER'] },
  { label: 'Factures', href: '/customer/bills', icon: FileText, roles: ['CUSTOMER'] },
  { label: 'Paiements', href: '/customer/payments', icon: CreditCard, roles: ['CUSTOMER'] },
  { label: 'Consommation', href: '/customer/consumption', icon: Zap, roles: ['CUSTOMER'] },
  { label: 'Incidents', href: '/customer/incidents', icon: AlertTriangle, roles: ['CUSTOMER'] },
  { label: 'Profil', href: '/customer/profile', icon: User, roles: ['CUSTOMER'] },

  // Agent items
  { label: 'Tableau de bord', href: '/agent/dashboard', icon: Gauge, roles: ['AGENT', 'ADMIN'] },
  { label: 'Abonnés', href: '/agent/subscribers', icon: Users, roles: ['AGENT', 'ADMIN'] },
  { label: 'Contrats', href: '/agent/contracts', icon: FileText, roles: ['AGENT', 'ADMIN'] },
  { label: 'Compteurs', href: '/agent/meters', icon: Zap, roles: ['AGENT', 'ADMIN'] },
  { label: 'Facturation', href: '/agent/billing', icon: CreditCard, roles: ['AGENT', 'ADMIN'] },
  { label: 'Incidents', href: '/agent/incidents', icon: AlertTriangle, roles: ['AGENT', 'ADMIN'] },
  { label: 'Réclamations', href: '/agent/complaints', icon: MessageSquare, roles: ['AGENT', 'ADMIN'] },

  // Technician items
  { label: 'Tableau de bord', href: '/technician/dashboard', icon: Gauge, roles: ['TECHNICIAN'] },
  { label: 'Mes tournées', href: '/technician/routes', icon: ClipboardList, roles: ['TECHNICIAN'] },
  { label: 'Interventions', href: '/technician/work-orders', icon: Building2, roles: ['TECHNICIAN'] },
  { label: 'Pannes', href: '/technician/outages', icon: AlertTriangle, roles: ['TECHNICIAN'] },

  // Admin items
  { label: 'Administration', href: '/admin/dashboard', icon: Settings, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { label: 'Utilisateurs', href: '/admin/users', icon: Users, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { label: 'Audit', href: '/admin/audit-logs', icon: ClipboardList, roles: ['ADMIN', 'SUPER_ADMIN'] },

  // Management items
  { label: 'Direction', href: '/management/dashboard', icon: Gauge, roles: ['MANAGER', 'ADMIN'] },
];

export function Sidebar() {
  const { hasAnyRole, logout } = useAuth();
  const location = useLocation();

  const filteredItems = navItems.filter((item) => hasAnyRole(item.roles));

  // Determine which section to show
  const currentRole = getCurrentRole(location.pathname);

  return (
    <aside className="fixed left-0 top-0 z-40 hidden h-screen w-64 flex-col border-r border-border bg-card lg:flex">
      {/* Logo */}
      <div className="flex h-16 items-center gap-3 border-b border-border px-6">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary">
          <span className="text-sm font-bold text-primary-foreground">J</span>
        </div>
        <div>
          <p className="text-sm font-semibold text-foreground">JIRAMA</p>
          <p className="text-xs text-muted-foreground">Portail Client</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto p-4">
        {currentRole && (
          <p className="mb-2 px-3 text-xs font-medium uppercase tracking-wider text-muted-foreground">
            {currentRole}
          </p>
        )}
        <ul className="space-y-1">
          {filteredItems.map((item) => (
            <li key={item.href}>
              <NavLink
                to={item.href}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-primary/10 text-primary'
                      : 'text-muted-foreground hover:bg-secondary hover:text-foreground',
                  )
                }
              >
                <item.icon className="h-4 w-4" />
                {item.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      {/* Logout */}
      <div className="border-t border-border p-4">
        <button
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground hover:bg-secondary hover:text-destructive transition-colors"
        >
          <LogOut className="h-4 w-4" />
          Déconnexion
        </button>
      </div>
    </aside>
  );
}

function getCurrentRole(path: string): string | null {
  if (path.startsWith('/customer')) return 'Client';
  if (path.startsWith('/agent')) return 'Agent';
  if (path.startsWith('/technician')) return 'Technicien';
  if (path.startsWith('/admin')) return 'Administration';
  if (path.startsWith('/management')) return 'Direction';
  return null;
}
