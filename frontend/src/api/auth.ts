import api from './axios';

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    password: string;
    email: string;
}

export const authApi = {
    login: async (data: LoginRequest) => {
        const response = await api.post('/api/auth/login', data);
        return response.data;
    },

    register: async (data: RegisterRequest) => {
        const response = await api.post('/api/auth/register', data);
        return response.data;
    },

    logout: async () => {
        const response = await api.post('/api/auth/logout');
        return response.data;
    },

    getCurrentUser: async () => {
        const response = await api.get('/api/auth/me');
        return response.data;
    }
}; 