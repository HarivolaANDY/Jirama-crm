import { Bell, LogOut, Moon, Sun, User } from 'lucide-react';
import { useAuth } from '@/providers/AuthProvider';
import { useTheme } from '@/providers/ThemeProvider';
import { APP } from '@/lib/constants';

interface TopbarProps {
  variant: 'public' | 'app';
}

export function Topbar({ variant }: TopbarProps) {
  const { isAuthenticated, user, logout } = useAuth();
  const { theme, setTheme } = useTheme();

  return (
    <header className="sticky top-0 z-50 border-b border-border bg-background/80 backdrop-blur-sm">
      <div className="flex h-16 items-center justify-between px-4 lg:px-6">
        {/* Left: Logo */}
        <div className="flex items-center gap-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
            <span className="text-sm font-bold text-primary-foreground">J</span>
          </div>
          <span className="text-lg font-semibold text-foreground">
            {APP.name}
          </span>
        </div>

        {/* Right: Actions */}
        <div className="flex items-center gap-2">
          {/* Theme toggle */}
          <button
            onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
            className="rounded-lg p-2 text-muted-foreground hover:bg-secondary hover:text-foreground transition-colors"
            aria-label="Toggle theme"
          >
            {theme === 'dark' ? (
              <Sun className="h-5 w-5" />
            ) : (
              <Moon className="h-5 w-5" />
            )}
          </button>

          {variant === 'app' && isAuthenticated && (
            <>
              {/* Notifications */}
              <button className="relative rounded-lg p-2 text-muted-foreground hover:bg-secondary hover:text-foreground transition-colors">
                <Bell className="h-5 w-5" />
                <span className="absolute right-1.5 top-1.5 flex h-2 w-2 rounded-full bg-destructive" />
              </button>

              {/* User menu */}
              <div className="flex items-center gap-2 pl-2 border-l border-border">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10">
                  <User className="h-4 w-4 text-primary" />
                </div>
                <span className="hidden text-sm font-medium text-foreground sm:inline">
                  {user?.firstName ?? 'Utilisateur'}
                </span>
                <button
                  onClick={logout}
                  className="rounded-lg p-2 text-muted-foreground hover:bg-secondary hover:text-destructive transition-colors"
                  aria-label="Logout"
                >
                  <LogOut className="h-4 w-4" />
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
