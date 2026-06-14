import { APP } from '@/lib/constants';

export function Footer() {
  return (
    <footer className="border-t border-border bg-card py-8">
      <div className="container mx-auto max-w-6xl px-4">
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
                <span className="text-sm font-bold text-primary-foreground">J</span>
              </div>
              <span className="text-sm font-semibold text-foreground">{APP.fullName}</span>
            </div>
            <p className="mt-2 text-xs text-muted-foreground leading-relaxed">
              {APP.tagline}
            </p>
          </div>

          {/* Services */}
          <div>
            <h3 className="mb-3 text-sm font-semibold text-foreground">Services</h3>
            <ul className="space-y-2 text-xs text-muted-foreground">
              <li><a href="#" className="hover:text-primary transition-colors">Électricité</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Eau</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Nouveau raccordement</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Devis en ligne</a></li>
            </ul>
          </div>

          {/* Support */}
          <div>
            <h3 className="mb-3 text-sm font-semibold text-foreground">Support</h3>
            <ul className="space-y-2 text-xs text-muted-foreground">
              <li><a href="#" className="hover:text-primary transition-colors">Centre d'aide</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Contact</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Agences</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Signalement</a></li>
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h3 className="mb-3 text-sm font-semibold text-foreground">Contact</h3>
            <ul className="space-y-2 text-xs text-muted-foreground">
              <li>📍 Antananarivo, Madagascar</li>
              <li>📞 100 (Service client)</li>
              <li>📧 contact@jirama.mg</li>
            </ul>
          </div>
        </div>

        <div className="mt-8 border-t border-border pt-6 text-center text-xs text-muted-foreground">
          <p>&copy; {new Date().getFullYear()} {APP.fullName}. Tous droits réservés.</p>
        </div>
      </div>
    </footer>
  );
}
