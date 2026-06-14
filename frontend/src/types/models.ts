/** ── Shared ── */
export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
}

/** ── Address ── */
export interface Address {
  line1: string;
  line2?: string;
  city: string;
  district?: string;
  regionCode?: string;
  postalCode?: string;
  latitude?: number;
  longitude?: number;
}

/** ── Subscriber ── */
export type SubscriberStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'BLACKLISTED';
export type SubscriberType = 'RESIDENTIAL' | 'COMMERCIAL' | 'INDUSTRIAL' | 'GOVERNMENT';

export interface Subscriber extends BaseEntity {
  subscriberNumber: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email?: string;
  phoneNumber: string;
  secondaryPhone?: string;
  address: Address;
  status: SubscriberStatus;
  subscriberType: SubscriberType;
  preferredLanguage: string;
}

/** ── Contract ── */
export type ContractStatus = 'ACTIVE' | 'SUSPENDED' | 'TERMINATED' | 'PENDING';
export type ContractType = 'ELECTRICITY' | 'WATER' | 'BOTH';

export interface Contract extends BaseEntity {
  contractNumber: string;
  subscriberId: string;
  contractType: ContractType;
  status: ContractStatus;
  startDate: string;
  endDate?: string;
  tariffCode: string;
  billingCycle: string;
}

/** ── Meter ── */
export type MeterStatus = 'ACTIVE' | 'INACTIVE' | 'FAULTY' | 'STOLEN' | 'REPLACED';
export type MeterType = 'ELECTRICITY' | 'WATER';

export interface Meter extends BaseEntity {
  meterNumber: string;
  contractId: string;
  meterType: MeterType;
  installationDate: string;
  lastReadingValue?: number;
  status: MeterStatus;
}

/** ── Invoice (Bill) ── */
export type InvoiceStatus = 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED' | 'PARTIALLY_PAID';

export interface Invoice extends BaseEntity {
  invoiceNumber: string;
  contractId: string;
  subscriberId: string;
  billingPeriodStart: string;
  billingPeriodEnd: string;
  issueDate: string;
  dueDate: string;
  status: InvoiceStatus;
  totalAmount: number;
  amountPaid: number;
  balanceDue: number;
  consumptionKwh?: number;
  consumptionM3?: number;
  pdfPath?: string;
}

/** ── Payment ── */
export type PaymentMethod = 'CASH' | 'CARD' | 'MOBILE_MONEY' | 'BANK_TRANSFER' | 'DIRECT_DEBIT' | 'ONLINE' | 'QR_CODE';
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED' | 'CANCELLED';

export interface Payment extends BaseEntity {
  paymentNumber: string;
  invoiceId: string;
  subscriberId: string;
  amount: number;
  paymentMethod: PaymentMethod;
  mobileMoneyProvider?: string;
  transactionReference?: string;
  status: PaymentStatus;
  receiptNumber?: string;
}

/** ── Incident ── */
export type IncidentType =
  | 'POWER_OUTAGE' | 'WATER_OUTAGE' | 'VOLTAGE_FLUCTUATION'
  | 'METER_MALFUNCTION' | 'LINE_BREAK' | 'TRANSFORMER_FAILURE'
  | 'WATER_LEAK' | 'LOW_PRESSURE' | 'OTHER';

export type IncidentSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
export type IncidentStatus =
  | 'REPORTED' | 'CONFIRMED' | 'ASSIGNED' | 'IN_PROGRESS'
  | 'RESOLVED' | 'CLOSED' | 'CANCELLED';

export interface Incident extends BaseEntity {
  incidentNumber: string;
  subscriberId?: string;
  incidentType: IncidentType;
  severity: IncidentSeverity;
  status: IncidentStatus;
  description: string;
  locationLat?: number;
  locationLng?: number;
  address?: string;
  resolutionNotes?: string;
  resolvedAt?: string;
}

/** ── Complaint ── */
export type ComplaintCategory =
  | 'BILLING' | 'METER' | 'SERVICE_QUALITY' | 'CUSTOMER_SERVICE'
  | 'CONNECTION' | 'DISCONNECTION' | 'OTHER';
export type ComplaintStatus =
  | 'OPEN' | 'IN_PROGRESS' | 'AWAITING_RESPONSE'
  | 'RESOLVED' | 'CLOSED' | 'ESCALATED';

export interface Complaint extends BaseEntity {
  complaintNumber: string;
  subscriberId: string;
  category: ComplaintCategory;
  subject: string;
  description: string;
  status: ComplaintStatus;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  assignedTo?: string;
  resolution?: string;
  satisfactionRating?: number;
}

/** ── User (Staff) ── */
export type UserRole = 'SUPER_ADMIN' | 'ADMIN' | 'AGENT' | 'TECHNICIAN' | 'CALL_CENTER' | 'MANAGER';

export interface User extends BaseEntity {
  employeeNumber: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  role: UserRole;
  isActive: boolean;
}

/** ── Notification ── */
export interface Notification {
  id: string;
  title: string;
  body: string;
  channel: 'EMAIL' | 'SMS' | 'PUSH' | 'IN_APP';
  status: 'PENDING' | 'SENT' | 'DELIVERED' | 'FAILED' | 'READ';
  readAt?: string;
  createdAt: string;
  referenceType?: string;
  referenceId?: string;
}

/** ── Document ── */
export interface Document {
  id: string;
  title: string;
  documentType: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  createdAt: string;
}

/** ── Pagination ── */
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
