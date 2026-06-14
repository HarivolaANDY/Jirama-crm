/** JIRAMA brand colors */
export const COLORS = {
  primary: '#0057B8',
  primaryLight: '#4686E7',
  primaryDark: '#00306A',
  energy: '#F59E0B',
  energyLight: '#F7AC4A',
  energyDark: '#935F07',
  success: '#22C55E',
  error: '#DC2626',
  warning: '#F59E0B',
  info: '#3B82F6',
} as const;

/** Application metadata */
export const APP = {
  name: 'JIRAMA',
  fullName: "Jiro sy Rano Malagasy",
  tagline: 'Ensemble, éclairons Madagascar',
  version: '1.0.0',
} as const;

/** API endpoints prefix */
export const API_PREFIX = '/api/v1';

/** Pagination defaults */
export const PAGINATION = {
  defaultPageSize: 20,
  maxPageSize: 100,
} as const;

/** Notification timing */
export const TOAST_DURATION = 5000;
export const QUERY_STALE_TIME = 30_000; // 30 seconds
export const QUERY_CACHE_TIME = 5 * 60_000; // 5 minutes

/** Role-based route prefixes */
export const ROUTE_PREFIXES = {
  customer: '/customer',
  agent: '/agent',
  technician: '/technician',
  admin: '/admin',
  management: '/management',
} as const;

/** Madagascar regions (Faritra) */
export const MADAGASCAR_REGIONS = [
  { code: 'ANA', name: 'Analamanga' },
  { code: 'BON', name: 'Bongolava' },
  { code: 'ITA', name: 'Itasy' },
  { code: 'VAK', name: 'Vakinankaratra' },
  { code: 'ATS', name: 'Atsinanana' },
  { code: 'ALA', name: 'Alaotra-Mangoro' },
  { code: 'ANO', name: 'Analanjirofo' },
  { code: 'BOE', name: 'Boeny' },
  { code: 'BET', name: 'Betsiboka' },
  { code: 'MEL', name: 'Melaky' },
  { code: 'SOF', name: 'Sofia' },
  { code: 'HAU', name: 'Haute Matsiatra' },
  { code: 'IHO', name: 'Ihorombe' },
  { code: 'AMO', name: "Amoron'i Mania" },
  { code: 'VAT', name: 'Vatovavy' },
  { code: 'FIT', name: 'Fitovinany' },
  { code: 'ATS_F', name: 'Atsimo-Atsinanana' },
  { code: 'ATS_T', name: 'Atsimo-Andrefana' },
  { code: 'AND', name: 'Androy' },
  { code: 'ANU', name: 'Anosy' },
  { code: 'MEN', name: 'Menabe' },
  { code: 'DIA', name: 'Diana' },
  { code: 'SAV', name: 'Sava' },
] as const;

export type RegionCode = (typeof MADAGASCAR_REGIONS)[number]['code'];
