import { apiClient } from './client';
import type { DashboardStats } from '@/types/api';

export const dashboardApi = {
  /** Get KPI dashboard statistics */
  getKpis: async (): Promise<DashboardStats> => {
    const response = await apiClient.get('/dashboard/kpi');
    return response.data;
  },

  /** Get revenue stats */
  getRevenue: async (period: 'daily' | 'weekly' | 'monthly' = 'monthly') => {
    const response = await apiClient.get('/dashboard/revenue', {
      params: { period },
    });
    return response.data;
  },

  /** Get regional statistics */
  getRegionalStats: async () => {
    const response = await apiClient.get('/dashboard/regional-stats');
    return response.data;
  },
};
