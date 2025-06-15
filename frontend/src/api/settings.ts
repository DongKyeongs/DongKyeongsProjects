import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const settingsApi = {
    getSettings: async (userId: string) => {
        const response = await axios.get(`${API_URL}/settings/${userId}`);
        return response.data;
    },

    updateSettings: async (userId: string, settings: any) => {
        const response = await axios.put(`${API_URL}/settings/${userId}`, settings);
        return response.data;
    },

    calculateFee: async (userId: string, amount: number, isMaker: boolean) => {
        const response = await axios.get(`${API_URL}/settings/${userId}/fee`, {
            params: { amount, isMaker }
        });
        return response.data;
    }
}; 