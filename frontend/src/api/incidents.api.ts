import { apiClient } from './client';
import type { Incident } from '@/types/models';
import type { ReportIncidentRequest, ReportIncidentResult } from '@/types/api';

export const incidentsApi = {
  /** Report a new incident */
  report: async (data: ReportIncidentRequest): Promise<ReportIncidentResult> => {
    const response = await apiClient.post('/incidents', data);
    return response.data;
  },

  /** Get incident by ID */
  getById: async (id: string): Promise<Incident> => {
    const response = await apiClient.get(`/incidents/${id}`);
    return response.data;
  },

  /** Get current user's incidents */
  getMyIncidents: async (page = 0, size = 20) => {
    const response = await apiClient.get('/incidents/my', {
      params: { page, size },
    });
    return response.data;
  },
};
