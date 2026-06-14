import { apiClient } from './client';

export const notificationsApi = {
  /** Get all notifications for the current user */
  getAll: async (page = 0, size = 50) => {
    const response = await apiClient.get('/notifications', {
      params: { page, size },
    });
    return response.data;
  },

  /** Get unread notification count */
  getUnreadCount: async (): Promise<number> => {
    const response = await apiClient.get('/notifications/unread-count');
    return response.data;
  },

  /** Mark a notification as read */
  markAsRead: async (id: string): Promise<void> => {
    await apiClient.patch(`/notifications/${id}/read`);
  },

  /** Mark all notifications as read */
  markAllAsRead: async (): Promise<void> => {
    await apiClient.patch('/notifications/read-all');
  },
};
