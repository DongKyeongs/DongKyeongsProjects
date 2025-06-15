import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const notificationApi = {
    getUserNotifications: async (userId: string) => {
        const response = await axios.get(`${API_URL}/notifications/${userId}`);
        return response.data;
    },

    createPriceAlert: async (userId: string, pair: string, targetPrice: number, isAbove: boolean) => {
        await axios.post(`${API_URL}/notifications/${userId}/price-alert`, null, {
            params: { pair, targetPrice, isAbove }
        });
    },

    markAsRead: async (userId: string, notificationId: number) => {
        await axios.post(`${API_URL}/notifications/${userId}/${notificationId}/read`);
    }
}; 