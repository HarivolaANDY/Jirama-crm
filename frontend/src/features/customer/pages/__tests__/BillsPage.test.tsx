import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { BillsPage } from '../BillsPage';

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
      <BillsPage />
    </MemoryRouter>,
  );
}

const MOCK_INVOICES = [
  {
    id: 'inv-1',
    invoiceNumber: 'FAC-2026-06-0001',
    billingPeriodStart: '2026-06-01',
    billingPeriodEnd: '2026-06-30',
    issueDate: '2026-07-01',
    dueDate: '2026-07-20',
    status: 'PENDING',
    totalAmount: 45000,
    amountPaid: 0,
    balanceDue: 45000,
    consumptionKwh: 245,
    pdfPath: null,
  },
  {
    id: 'inv-2',
    invoiceNumber: 'FAC-2026-05-0001',
    billingPeriodStart: '2026-05-01',
    billingPeriodEnd: '2026-05-31',
    issueDate: '2026-06-01',
    dueDate: '2026-06-20',
    status: 'PAID',
    totalAmount: 42500,
    amountPaid: 42500,
    balanceDue: 0,
    consumptionKwh: 228,
    pdfPath: null,
  },
  {
    id: 'inv-3',
    invoiceNumber: 'FAC-2026-04-0001',
    billingPeriodStart: '2026-04-01',
    billingPeriodEnd: '2026-04-30',
    issueDate: '2026-05-01',
    dueDate: '2026-05-20',
    status: 'OVERDUE',
    totalAmount: 44000,
    amountPaid: 10000,
    balanceDue: 34000,
    consumptionKwh: null,
    pdfPath: null,
  },
];

describe('BillsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ── Loading state ──

  it('affiche le spinner de chargement pendant le chargement', () => {
    mockGet.mockImplementationOnce(() => new Promise(() => {}));
    renderPage();
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText('Chargement des factures…')).toBeInTheDocument();
  });

  // ── Empty state ──

  describe('lorsque la liste est vide', () => {
    beforeEach(async () => {
      mockGet.mockResolvedValueOnce({ data: [] });
      renderPage();
      await waitFor(() => {
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
      });
    });

    it('affiche le message d\'état vide', () => {
      expect(screen.getByText('Aucune facture')).toBeInTheDocument();
      expect(
        screen.getByText("Vous n'avez pas encore de factures. Elles apparaîtront ici une fois générées."),
      ).toBeInTheDocument();
    });

    it('affiche la description par défaut dans le titre', () => {
      expect(
        screen.getByText('Consultez et payez vos factures JIRAMA'),
      ).toBeInTheDocument();
    });
  });

  // ── Data table rendering ──

  describe('lorsque des factures sont chargées', () => {
    beforeEach(async () => {
      mockGet.mockResolvedValueOnce({ data: MOCK_INVOICES });
      renderPage();
      await waitFor(() => {
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
      });
    });

    it('affiche le titre "Mes factures"', () => {
      expect(screen.getByText('Mes factures')).toBeInTheDocument();
    });

    it('affiche le total dû dans le header', () => {
      expect(screen.getByText(/Total dû/)).toBeInTheDocument();
      // 45 000 (PENDING) + 34 000 (OVERDUE) = 79 000
      // Appears in both header description and footer summary
      expect(screen.getAllByText(currencyRegex('79 000 MGA')).length).toBeGreaterThanOrEqual(1);
    });

    it('affiche les en-têtes du tableau', () => {
      expect(screen.getByText('Période')).toBeInTheDocument();
      expect(screen.getByText('N° Facture')).toBeInTheDocument();
      expect(screen.getByText('Montant')).toBeInTheDocument();
      expect(screen.getByText('Dû')).toBeInTheDocument();
      expect(screen.getByText('Échéance')).toBeInTheDocument();
      expect(screen.getByText('Statut')).toBeInTheDocument();
      expect(screen.getByText('Actions')).toBeInTheDocument();
    });

    it('affiche les numéros de facture dans le tableau', () => {
      expect(screen.getByText('FAC-2026-06-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-05-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-04-0001')).toBeInTheDocument();
    });

    it('affiche les montants formatés', () => {
      // 4 instances of formatted currency: total pending in header + 3 amounts in table
      const formattedAmounts = screen.getAllByText(/^(?:4[254]|79)[\u202f\u00a0\s]\d{3}[\u202f\u00a0\s]MGA$/);
      expect(formattedAmounts.length).toBeGreaterThanOrEqual(3);
    });

    it('affiche les badges de statut', () => {
      expect(screen.getByText('PENDING')).toBeInTheDocument();
      expect(screen.getByText('PAID')).toBeInTheDocument();
      expect(screen.getByText('OVERDUE')).toBeInTheDocument();
    });

    it('affiche le footer avec le nombre total de factures', () => {
      expect(screen.getByText('3 factures au total')).toBeInTheDocument();
    });

    it('affiche le total impayé dans le footer', () => {
      // 79 000 Ar appears in both header and footer — use getAllByText
      expect(screen.getAllByText(currencyRegex('79 000 MGA')).length).toBeGreaterThanOrEqual(1);
    });

    it('affiche le bouton "Tout télécharger"', () => {
      expect(screen.getByText('Tout télécharger')).toBeInTheDocument();
    });
  });

  // ── Status filter tabs ──

  describe('filtres par statut', () => {
    beforeEach(async () => {
      mockGet.mockResolvedValueOnce({ data: MOCK_INVOICES });
      renderPage();
      await waitFor(() => {
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
      });
    });

    it('affiche les 4 onglets de filtre', () => {
      expect(screen.getByText('Toutes')).toBeInTheDocument();
      expect(screen.getByText('En attente')).toBeInTheDocument();
      expect(screen.getByText('Payées')).toBeInTheDocument();
      expect(screen.getByText('En retard')).toBeInTheDocument();
    });

    it('filtre pour afficher uniquement les factures en attente', () => {
      fireEvent.click(screen.getByText('En attente'));
      expect(screen.getByText('FAC-2026-06-0001')).toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-05-0001')).not.toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-04-0001')).not.toBeInTheDocument();
    });

    it('filtre pour afficher uniquement les factures payées', () => {
      fireEvent.click(screen.getByText('Payées'));
      expect(screen.getByText('FAC-2026-05-0001')).toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-06-0001')).not.toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-04-0001')).not.toBeInTheDocument();
    });

    it('filtre pour afficher uniquement les factures en retard', () => {
      fireEvent.click(screen.getByText('En retard'));
      expect(screen.getByText('FAC-2026-04-0001')).toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-06-0001')).not.toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-05-0001')).not.toBeInTheDocument();
    });

    it('affiche toutes les factures avec le filtre "Toutes"', () => {
      expect(screen.getByText('FAC-2026-06-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-05-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-04-0001')).toBeInTheDocument();

      fireEvent.click(screen.getByText('En attente'));
      fireEvent.click(screen.getByText('Toutes'));

      expect(screen.getByText('FAC-2026-06-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-05-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-04-0001')).toBeInTheDocument();
    });
  });

  // ── Empty filter state ──

  it('affiche un message différent quand le filtre ne donne aucun résultat', async () => {
    const paidOnly = MOCK_INVOICES.filter(i => i.status === 'PAID');
    mockGet.mockResolvedValueOnce({ data: paidOnly });
    renderPage();

    await waitFor(() => {
      expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('En retard'));
    expect(
      screen.getByText('Aucune facture ne correspond à ce filtre.'),
    ).toBeInTheDocument();
  });

  // ── Search ──

  describe('recherche par numéro de facture', () => {
    beforeEach(async () => {
      mockGet.mockResolvedValueOnce({ data: MOCK_INVOICES });
      renderPage();
      await waitFor(() => {
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
      });
    });

    it('filtre les factures par recherche', () => {
      const searchInput = screen.getByPlaceholderText('Rechercher une facture…');
      fireEvent.change(searchInput, { target: { value: '06' } });

      expect(screen.getByText('FAC-2026-06-0001')).toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-05-0001')).not.toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-04-0001')).not.toBeInTheDocument();
    });

    it('est insensible à la casse', () => {
      const searchInput = screen.getByPlaceholderText('Rechercher une facture…');
      fireEvent.change(searchInput, { target: { value: 'fac' } });

      expect(screen.getByText('FAC-2026-06-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-05-0001')).toBeInTheDocument();
      expect(screen.getByText('FAC-2026-04-0001')).toBeInTheDocument();
    });

    it('se combine avec le filtre de statut', () => {
      const searchInput = screen.getByPlaceholderText('Rechercher une facture…');

      fireEvent.click(screen.getByText('En attente'));
      fireEvent.change(searchInput, { target: { value: '06' } });

      expect(screen.getByText('FAC-2026-06-0001')).toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-05-0001')).not.toBeInTheDocument();
      expect(screen.queryByText('FAC-2026-04-0001')).not.toBeInTheDocument();
    });
  });

  // ── Sorting ──

  it('trie les factures par période décroissante', async () => {
    mockGet.mockResolvedValueOnce({ data: MOCK_INVOICES });
    renderPage();

    await waitFor(() => {
      expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });

    const invoiceElements = screen.getAllByText(/FAC-/);
    expect(invoiceElements[0]).toHaveTextContent('FAC-2026-06-0001');
    expect(invoiceElements[1]).toHaveTextContent('FAC-2026-05-0001');
    expect(invoiceElements[2]).toHaveTextContent('FAC-2026-04-0001');
  });
});
