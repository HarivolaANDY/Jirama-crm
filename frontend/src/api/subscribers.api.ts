import { apiClient } from './client';
import type { Subscriber } from '@/types/models';
import type { CreateSubscriberRequest, CreateSubscriberResult } from '@/types/api';

export const subscribersApi = {
  /** Create a new subscriber */
  create: async (data: CreateSubscriberRequest): Promise<CreateSubscriberResult> => {
    const response = await apiClient.post('/subscribers', data);
    return response.data;
  },

  /** Get subscriber by ID */
  getById: async (id: string): Promise<Subscriber> => {
    const response = await apiClient.get(`/subscribers/${id}`);
    return response.data;
  },

  /** Get current user's subscriber profile */
  getMyProfile: async (): Promise<Subscriber> => {
    const response = await apiClient.get('/subscribers/my');
    return response.data;
  },

  /** Search subscribers */
  search: async (query: string, page = 0, size = 20) => {
    const response = await apiClient.get('/subscribers', {
      params: { q: query, page, size },
    });
    return response.data;
  },
};
