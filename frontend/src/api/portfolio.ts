import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const portfolioApi = {
    getUserPortfolios: async (userId: string) => {
        const response = await axios.get(`${API_URL}/portfolio/${userId}`);
        return response.data;
    },

    getPortfolio: async (userId: string, asset: string) => {
        const response = await axios.get(`${API_URL}/portfolio/${userId}/${asset}`);
        return response.data;
    }
}; 