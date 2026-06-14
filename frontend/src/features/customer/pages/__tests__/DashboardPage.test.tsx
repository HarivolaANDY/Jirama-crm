import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { DashboardPage } from '../DashboardPage';

// ── Mocks ──

const mockGet = vi.fn();

vi.mock('@/api/client', () => ({
  apiClient: {
    get: (...args: unknown[]) => mockGet(...args),
  },
}));

vi.mock('@/providers/AuthProvider', () => ({
  useAuth: () => ({ user: { firstName: 'Jean' } }),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual };
});

// ── Helpers ──

/** Matches currency values formatted by Intl.NumberFormat (handles non-breaking spaces) */
const currencyRegex = (amount: string) => new RegExp(
  amount.replace(/ /g, '[\\u202f\\u00a0\\s]'),
);

function renderPage() {
  return render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>,
  );
}

const MOCK_DASHBOARD_DATA = {
  currentBill: {
    id: 'bill-1',
    invoiceNumber: 'FAC-2026-06-0001',
    totalAmount: 45000,
    balanceDue: 45000,
    dueDate: '2026-07-15',
    isOverdue: true,
  },
  recentInvoices: [
    {
      id: 'inv-1',
      invoiceNumber: 'FAC-2026-06-0001',
      periodLabel: 'Juin 2026',
      totalAmount: 45000,
      status: 'PENDING',
    },
    {
      id: 'inv-2',
      invoiceNumber: 'FAC-2026-05-0001',
      periodLabel: 'Mai 2026',
      totalAmount: 42500,
      status: 'PAID',
    },
    {
      id: 'inv-3',
      invoiceNumber: 'FAC-2026-04-0001',
      periodLabel: 'Avril 2026',
      totalAmount: 44000,
      status: 'PAID',
    },
  ],
  consumption: {
    currentKwh: 245,
    previousKwh: 228,
    changePercent: 7.5,
  },
  openIncidents: 1,
};

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ── Loading state ──

  it('affiche le spinner de chargement pendant le chargement des données', () => {
    mockGet.mockImplementationOnce(() => new Promise(() => {}));
    renderPage();
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText('Chargement du tableau de bord…')).toBeInTheDocument();
  });

  // ── Data loaded with content ──

  describe('lorsque les données sont chargées', () => {
    beforeEach(async () => {
      mockGet.mockResolvedValueOnce({ data: MOCK_DASHBOARD_DATA });
      renderPage();
      await waitFor(() => {
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
      });
    });

    it('affiche le message de bienvenue avec le prénom', () => {
      expect(screen.getByText('Bonjour, Jean')).toBeInTheDocument();
    });

    it('affiche les 4 cartes KPI', () => {
      expect(screen.getByText('Facture en cours')).toBeInTheDocument();
      expect(screen.getByText('Consommation du mois')).toBeInTheDocument();
      expect(screen.getByText('Paiements effectués')).toBeInTheDocument();
      expect(screen.getByText('Incidents en cours')).toBeInTheDocument();
    });

    it('affiche le montant de la facture en cours formaté', () => {
      expect(screen.getAllByText(currencyRegex('45 000 MGA')).length).toBeGreaterThanOrEqual(1);
    });

    it('affiche la consommation du mois', () => {
      expect(screen.getByText('245 kWh')).toBeInTheDocument();
    });

    it("affiche le nombre d'incidents en cours", () => {
      expect(screen.getByText('1')).toBeInTheDocument();
    });

    it('affiche la liste des factures récentes', () => {
      expect(screen.getByText('Juin 2026')).toBeInTheDocument();
      expect(screen.getByText('Mai 2026')).toBeInTheDocument();
      expect(screen.getByText('Avril 2026')).toBeInTheDocument();
    });

    it("affiche les numéros de facture", () => {
      expect(screen.getByText('FAC-2026-06-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-05-0001')).toBeInTheDocument();
    });

    it('affiche les badges de statut pour chaque facture', () => {
      const paidBadges = screen.getAllByText('PAID');
      expect(paidBadges).toHaveLength(2);
    });

    it("affiche le lien 'Voir tout' vers la page des factures", () => {
      const link = screen.getByText('Voir tout');
      expect(link.closest('a')).toHaveAttribute('href', '/customer/bills');
    });
  });

  // ── Actions rapides ──

  describe('actions rapides', () => {
    beforeEach(async () => {
      mockGet.mockResolvedValueOnce({ data: MOCK_DASHBOARD_DATA });
      renderPage();
      await waitFor(() => {
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
      });
    });

    it("affiche le titre 'Actions rapides'", () => {
      expect(screen.getByText('Actions rapides')).toBeInTheDocument();
    });

    it("affiche le lien 'Payer ma facture'", () => {
      const link = screen.getByText('Payer ma facture');
      expect(link.closest('a')).toHaveAttribute('href', '/customer/bills');
    });

    it("affiche le lien 'Voir ma consommation'", () => {
      const link = screen.getByText('Voir ma consommation');
      expect(link.closest('a')).toHaveAttribute('href', '/customer/consumption');
    });

    it("affiche le lien 'Signaler un incident'", () => {
      const link = screen.getByText('Signaler un incident');
      expect(link.closest('a')).toHaveAttribute('href', '/customer/incidents');
    });
  });

  // ── Empty / fallback data ──

  describe('lorsque les données sont vides', () => {
    beforeEach(async () => {
      mockGet.mockResolvedValueOnce({ data: null });
      renderPage();
      await waitFor(() => {
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
      });
    });

    it('affiche les valeurs par défaut pour les KPI', () => {
      expect(screen.getByText((content) => content.includes('0') && content.includes('Ar'))).toBeInTheDocument();
      expect(screen.getByText('—')).toBeInTheDocument();
      const zeroCounts = screen.getAllByText('0');
      expect(zeroCounts).toHaveLength(2);
    });

    it('affiche l\'état vide dans la section factures', () => {
      expect(screen.getByText('Aucune facture')).toBeInTheDocument();
    });
  });

  // ── API error ──

  it('affiche le tableau de bord avec des valeurs vides en cas d\'erreur API', async () => {
    mockGet.mockRejectedValueOnce(new Error('Network error'));
    renderPage();

    await waitFor(() => {
      expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });

    expect(screen.getByText((content) => content.includes('0') && content.includes('Ar'))).toBeInTheDocument();
    expect(screen.getByText('Aucune facture')).toBeInTheDocument();
  });
});
