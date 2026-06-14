import { apiClient } from './client';
import type { Invoice } from '@/types/models';

export const invoicesApi = {
  /** Get invoice by ID */
  getById: async (id: string): Promise<Invoice> => {
    const response = await apiClient.get(`/invoices/${id}`);
    return response.data;
  },

  /** Get current user's unpaid invoices */
  getMyCurrent: async (): Promise<Invoice[]> => {
    const response = await apiClient.get('/invoices/my/current');
    return response.data;
  },

  /** Get current user's invoice history */
  getMyInvoices: async (page = 0, size = 20) => {
    const response = await apiClient.get('/invoices/my', {
      params: { page, size },
    });
    return response.data;
  },

  /** Download invoice PDF */
  downloadPdf: async (id: string): Promise<Blob> => {
    const response = await apiClient.get(`/invoices/${id}/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },
};
