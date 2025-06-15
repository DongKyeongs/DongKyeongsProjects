import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const chartApi = {
    getChartData: async (pair: string, interval: string = '1h', limit: number = 100) => {
        const response = await axios.get(`${API_URL}/chart/${pair}`, {
            params: { interval, limit }
        });
        return response.data;
    }
}; 