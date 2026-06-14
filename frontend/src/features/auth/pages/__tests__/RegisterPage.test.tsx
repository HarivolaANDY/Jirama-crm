import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { RegisterPage } from '../RegisterPage';

// ── Mocks ──

const mockNavigate = vi.fn();
let mockAuthState: {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: { firstName: string } | null;
} = {
  isAuthenticated: false,
  isLoading: false,
  user: null,
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

const mockRegister = vi.fn();
vi.mock('@/api/auth.api', () => ({
  authApi: {
    register: (...args: unknown[]) => mockRegister(...args),
  },
}));

vi.mock('@/api/client', () => ({
  getErrorMessage: (err: unknown) =>
    err instanceof Error ? err.message : 'Erreur inconnue.',
}));

// ── Helpers ──

function renderPage() {
  return render(
    <MemoryRouter>
      <RegisterPage />
    </MemoryRouter>,
  );
}

/** Fill all required form fields with the given data and submit */
function fillField(label: string, value: string) {
  const input = screen.getByLabelText(label) as HTMLInputElement | HTMLSelectElement;
  fireEvent.change(input, { target: { value } });
}

function submitForm() {
  fireEvent.click(screen.getByText('Créer mon compte'));
}

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockAuthState = {
      isAuthenticated: false,
      isLoading: false,
      user: null,
    };
  });

  // ── Loading state ──

  it('affiche le spinner de chargement pendant la vérification de la session', () => {
    mockAuthState = { isAuthenticated: false, isLoading: true, user: null };
    renderPage();
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText('Vérification de votre session…')).toBeInTheDocument();
  });

  // ── Already authenticated ──

  describe('lorsque déjà authentifié', () => {
    beforeEach(() => {
      mockAuthState = {
        isAuthenticated: true,
        isLoading: false,
        user: { firstName: 'Jean' },
      };
    });

    it("affiche un message de bienvenue avec le prénom de l'utilisateur", () => {
      renderPage();
      expect(screen.getByText('Bienvenue Jean !')).toBeInTheDocument();
    });

    it('affiche un bouton redirigeant vers le tableau de bord', () => {
      renderPage();
      expect(screen.getByText('Tableau de bord')).toBeInTheDocument();
    });

    it('navigue vers /customer/dashboard au clic sur le bouton', () => {
      renderPage();
      fireEvent.click(screen.getByText('Tableau de bord'));
      expect(mockNavigate).toHaveBeenCalledWith('/customer/dashboard');
    });
  });

  // ── Registration form rendering ──

  describe('formulaire d\'inscription', () => {
    it('affiche le titre de la page', () => {
      renderPage();
      expect(screen.getByText('Finalisez votre inscription')).toBeInTheDocument();
    });

    it('affiche tous les champs requis', () => {
      renderPage();
      expect(screen.getByLabelText('Prénom *')).toBeInTheDocument();
      expect(screen.getByLabelText('Nom *')).toBeInTheDocument();
      expect(screen.getByLabelText('Email *')).toBeInTheDocument();
      expect(screen.getByLabelText('Téléphone *')).toBeInTheDocument();
      expect(screen.getByLabelText('Adresse *')).toBeInTheDocument();
      expect(screen.getByLabelText('Ville *')).toBeInTheDocument();
      expect(screen.getByLabelText('Région')).toBeInTheDocument();
    });

    it('affiche le champ optionnel Complément d\'adresse', () => {
      renderPage();
      expect(screen.getByLabelText('Complément d\'adresse')).toBeInTheDocument();
    });

    it('affiche les options de région', () => {
      renderPage();
      const regionSelect = screen.getByLabelText('Région');
      expect(regionSelect).toHaveDisplayValue('Sélectionnez…');
    });

    it('affiche le bouton de soumission', () => {
      renderPage();
      expect(screen.getByText('Créer mon compte')).toBeInTheDocument();
    });

    it('affiche un lien vers la page de connexion', () => {
      renderPage();
      const loginLink = screen.getByText('Connectez-vous');
      expect(loginLink).toBeInTheDocument();
      expect(loginLink.closest('a')).toHaveAttribute('href', '/login');
    });

    it('affiche le texte "Déjà inscrit ?" avant le lien de connexion', () => {
      renderPage();
      expect(screen.getByText('Déjà inscrit ?')).toBeInTheDocument();
    });
  });

  // ── Form field interaction ──

  describe('interaction avec les champs', () => {
    it('met à jour le champ prénom lors de la saisie', () => {
      renderPage();
      const input = screen.getByLabelText('Prénom *') as HTMLInputElement;
      fireEvent.change(input, { target: { value: 'Jean' } });
      expect(input.value).toBe('Jean');
    });

    it('met à jour le champ téléphone lors de la saisie', () => {
      renderPage();
      const input = screen.getByLabelText('Téléphone *') as HTMLInputElement;
      fireEvent.change(input, { target: { value: '+261341234567' } });
      expect(input.value).toBe('+261341234567');
    });
  });

  // ── Form submission ──

  describe('soumission du formulaire', () => {
    const validFormData = {
      firstName: 'Jean',
      lastName: 'Rakoto',
      email: 'jean.rakoto@email.com',
      phoneNumber: '+261341234567',
      addressLine1: 'Lot IVK 123',
      addressLine2: '',
      city: 'Antananarivo',
      district: 'Ambohimanarina',
      regionCode: 'ANA',
    };

    function fillAllFields() {
      fillField('Prénom *', validFormData.firstName);
      fillField('Nom *', validFormData.lastName);
      fillField('Email *', validFormData.email);
      fillField('Téléphone *', validFormData.phoneNumber);
      fillField('Adresse *', validFormData.addressLine1);
      fillField('Ville *', validFormData.city);
      fillField('District', validFormData.district);
      fillField('Région', validFormData.regionCode);
    }

    it('appelle authApi.register avec les données du formulaire en cas de succès', async () => {
      mockRegister.mockResolvedValueOnce({
        id: 'abc-123',
        subscriberNumber: 'JRM-2026-000001',
        fullName: 'Jean Rakoto',
      });

      renderPage();
      fillAllFields();
      submitForm();

      await waitFor(() => {
        expect(mockRegister).toHaveBeenCalledWith({
          firstName: 'Jean',
          lastName: 'Rakoto',
          email: 'jean.rakoto@email.com',
          phoneNumber: '+261341234567',
          addressLine1: 'Lot IVK 123',
          addressLine2: '',
          city: 'Antananarivo',
          district: 'Ambohimanarina',
          regionCode: 'ANA',
        });
      });
    });

    it('navigue vers /customer/dashboard avec les infos de registration en cas de succès', async () => {
      mockRegister.mockResolvedValueOnce({
        id: 'abc-123',
        subscriberNumber: 'JRM-2026-000001',
        fullName: 'Jean Rakoto',
      });

      renderPage();
      fillAllFields();
      submitForm();

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/customer/dashboard', {
          state: {
            registrationComplete: true,
            subscriberNumber: 'JRM-2026-000001',
          },
        });
      });
    });

    it('affiche un message d\'erreur en cas d\'échec de l\'inscription', async () => {
      mockRegister.mockRejectedValueOnce(new Error('Un abonné avec ce numéro existe déjà'));

      renderPage();
      fillAllFields();
      submitForm();

      await waitFor(() => {
        expect(
          screen.getByText('Un abonné avec ce numéro existe déjà'),
        ).toBeInTheDocument();
      });
    });

    it('désactive le bouton et change le texte pendant la soumission', async () => {
      // Return a promise that never resolves to keep isSubmitting=true
      mockRegister.mockImplementationOnce(() => new Promise(() => {}));

      renderPage();
      fillAllFields();
      submitForm();

      await waitFor(() => {
        expect(screen.getByText('Inscription en cours…')).toBeInTheDocument();
      });
    });
  });

  // ── Error display ──

  it('affiche une erreur réseau avec le bon message', async () => {
    const networkError = new Error('Erreur réseau. Vérifiez votre connexion.');
    mockRegister.mockRejectedValueOnce(networkError);

    renderPage();
    fillField('Prénom *', 'Jean');
    fillField('Nom *', 'Rakoto');
    fillField('Email *', 'jean@email.com');
    fillField('Téléphone *', '+261341234567');
    fillField('Adresse *', 'Addr');
    fillField('Ville *', 'Tana');
    submitForm();

    await waitFor(() => {
      expect(
        screen.getByText('Erreur réseau. Vérifiez votre connexion.'),
      ).toBeInTheDocument();
    });
  });
});
