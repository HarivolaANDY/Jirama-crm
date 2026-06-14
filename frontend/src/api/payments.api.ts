import { apiClient } from './client';
import type { Payment } from '@/types/models';
import type { ProcessPaymentRequest, ProcessPaymentResult } from '@/types/api';

export const paymentsApi = {
  /** Process a payment against an invoice */
  process: async (data: ProcessPaymentRequest): Promise<ProcessPaymentResult> => {
    const response = await apiClient.post('/payments', data);
    return response.data;
  },

  /** Get current user's payment history */
  getMyPayments: async (page = 0, size = 20) => {
    const response = await apiClient.get('/payments/my', {
      params: { page, size },
    });
    return response.data;
  },

  /** Get available payment methods */
  getMethods: async (): Promise<{
    methods: string[];
    mobileMoneyProviders: string[];
  }> => {
    const response = await apiClient.get('/payments/methods');
    return response.data;
  },
};
