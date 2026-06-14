/** ── API Error Response ── */
export interface ApiError {
  status: number;
  code: string;
  message: string;
  timestamp: string;
  path: string;
  errors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}

/** ── Subscriber API ── */
export interface CreateSubscriberRequest {
  firstName: string;
  lastName: string;
  email?: string;
  phoneNumber: string;
  idCardNumber?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  district?: string;
  regionCode?: string;
  postalCode?: string;
  subscriberType: string;
  preferredLanguage?: string;
}

export interface CreateSubscriberResult {
  id: string;
  subscriberNumber: string;
  fullName: string;
  email: string;
}

/** ── Payment API ── */
export interface ProcessPaymentRequest {
  invoiceId: string;
  amount: number;
  paymentMethod: string;
  mobileMoneyProvider?: string;
  phoneNumber?: string;
}

export interface ProcessPaymentResult {
  paymentId: string;
  paymentNumber: string;
  status: string;
  message: string;
}

/** ── Incident API ── */
export interface ReportIncidentRequest {
  subscriberId?: string;
  incidentType: string;
  severity: string;
  description: string;
  locationLat?: number;
  locationLng?: number;
  address?: string;
  regionId?: string;
}

export interface ReportIncidentResult {
  id: string;
  incidentNumber: string;
  status: string;
  message: string;
}

/** ── Dashboard Stats ── */
export interface DashboardStats {
  totalSubscribers: number;
  activeSubscribers: number;
  totalInvoicesMonth: number;
  collectedRevenue: number;
  pendingRevenue: number;
  openIncidents: number;
  collectionRate: number;
  subscriberGrowth: number;
}
