import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { LoginPage } from '../LoginPage';

// ── Mocks ──

const mockNavigate = vi.fn();
const mockLogin = vi.fn();

let mockAuthState: {
  isAuthenticated: boolean;
  isLoading: boolean;
  login: () => void;
} = {
  isAuthenticated: false,
  isLoading: false,
  login: mockLogin,
};

vi.mock('@/providers/AuthProvider', () => ({
  useAuth: () => mockAuthState,
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// ── Helpers ──

function renderPage() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockAuthState = {
      isAuthenticated: false,
      isLoading: false,
      login: mockLogin,
    };
  });

  // ── Loading state ──

  it('affiche le spinner de chargement pendant la vérification', () => {
    mockAuthState = { isAuthenticated: false, isLoading: true, login: mockLogin };
    renderPage();
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText('Chargement…')).toBeInTheDocument();
  });

  // ── Already authenticated → redirect ──

  it('redirige vers le tableau de bord lorsque déjà authentifié', () => {
    mockAuthState = { isAuthenticated: true, isLoading: false, login: mockLogin };
    renderPage();

    expect(mockNavigate).toHaveBeenCalledWith('/customer/dashboard', {
      replace: true,
    });
  });

  it('ne redirige pas pendant le chargement', () => {
    mockAuthState = { isAuthenticated: true, isLoading: true, login: mockLogin };
    renderPage();

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('ne redirige pas si non authentifié', () => {
    renderPage();

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  // ── Login card rendering ──

  describe('affichage de la carte de connexion', () => {
    beforeEach(() => {
      renderPage();
    });

    it('affiche le titre de la page', () => {
      expect(screen.getByText('Connexion')).toBeInTheDocument();
    });

    it('affiche le nom complet de l\'application', () => {
      expect(screen.getByText(/Jiro sy Rano Malagasy/)).toBeInTheDocument();
    });

    it('affiche le bouton de connexion SSO', () => {
      expect(
        screen.getByText('Se connecter avec JIRAMA'),
      ).toBeInTheDocument();
    });

    it('affiche le texte d\'information sur la redirection', () => {
      expect(
        screen.getByText('Vous serez redirigé vers la page de connexion sécurisée'),
      ).toBeInTheDocument();
    });

    it('affiche un lien vers la page d\'inscription', () => {
      const registerLink = screen.getByText('Créez-en un');
      expect(registerLink).toBeInTheDocument();
      expect(registerLink.closest('a')).toHaveAttribute('href', '/register');
    });
  });

  // ── Login button interaction ──

  it('appelle la fonction login() lors du clic sur le bouton SSO', () => {
    renderPage();
    fireEvent.click(screen.getByText('Se connecter avec JIRAMA'));
    expect(mockLogin).toHaveBeenCalledOnce();
  });

  // ── Navigation link ──

  it('le lien d\'inscription pointe vers /register', () => {
    renderPage();
    const link = screen.getByText('Créez-en un');
    expect(link.closest('a')).toHaveAttribute('href', '/register');
  });
});
