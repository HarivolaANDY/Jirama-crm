import { NavLink } from 'react-router-dom';
import { Home, FileText, Bell, User, Zap } from 'lucide-react';
import { cn } from '@/lib/utils';

const mobileNavItems = [
  { label: 'Accueil', href: '/customer/dashboard', icon: Home },
  { label: 'Factures', href: '/customer/bills', icon: FileText },
  { label: 'Consommation', href: '/customer/consumption', icon: Zap },
  { label: 'Profil', href: '/customer/profile', icon: User },
  { label: 'Paiements', href: '/customer/payments', icon: Bell },
];

/**
 * Bottom navigation bar for mobile devices.
 */
export function MobileNav() {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 border-t border-border bg-background lg:hidden">
      <div className="flex items-center justify-around py-2">
        {mobileNavItems.map((item) => (
          <NavLink
            key={item.href}
            to={item.href}
            className={({ isActive }) =>
              cn(
                'flex flex-col items-center gap-1 px-3 py-1 text-xs font-medium transition-colors',
                isActive
                  ? 'text-primary'
                  : 'text-muted-foreground hover:text-foreground',
              )
            }
          >
            <item.icon className="h-5 w-5" />
            {item.label}
          </NavLink>
        ))}
      </div>
    </nav>
  );
}
