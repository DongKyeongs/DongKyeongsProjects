import axiosInstance from './axios';

// 사용자 관련 API
export const userService = {
    login: (username: string, password: string) => 
        axiosInstance.post('/api/auth/login', { username, password }),
    
    register: (userData: any) => 
        axiosInstance.post('/api/auth/register', userData),
    
    getProfile: () => 
        axiosInstance.get('/api/user/profile'),
    
    updateProfile: (userData: any) => 
        axiosInstance.put('/api/user/profile', userData)
};

// 거래 관련 API
export const tradingService = {
    getMarketPrice: (symbol: string) => 
        axiosInstance.get(`/api/market/price/${symbol}`),
    
    getOrderBook: (symbol: string) => 
        axiosInstance.get(`/api/market/orderbook/${symbol}`),
    
    placeOrder: (orderData: any) => 
        axiosInstance.post('/api/trading/order', orderData),
    
    getOrders: (params: any) => 
        axiosInstance.get('/api/trading/orders', { params }),
    
    cancelOrder: (orderId: string) => 
        axiosInstance.delete(`/api/trading/order/${orderId}`)
};

// 지갑 관련 API
export const walletService = {
    getBalance: () => 
        axiosInstance.get('/api/wallet/balance'),
    
    getDepositAddress: (currency: string) => 
        axiosInstance.get(`/api/wallet/deposit/${currency}`),
    
    withdraw: (withdrawData: any) => 
        axiosInstance.post('/api/wallet/withdraw', withdrawData),
    
    getTransactionHistory: (params: any) => 
        axiosInstance.get('/api/wallet/transactions', { params })
};

// 차트 데이터 API
export const chartService = {
    getCandles: (symbol: string, interval: string, params: any) => 
        axiosInstance.get(`/api/chart/candles/${symbol}/${interval}`, { params }),
    
    getIndicators: (symbol: string, indicator: string, params: any) => 
        axiosInstance.get(`/api/chart/indicators/${symbol}/${indicator}`, { params })
}; 