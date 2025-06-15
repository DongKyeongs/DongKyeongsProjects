import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export interface PriceData {
  symbol: string;
  price: number;
  change: number;
  volume: number;
}

export interface ChartData {
  time: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export const getPrices = async (): Promise<PriceData[]> => {
  const response = await axios.get(`${API_BASE_URL}/prices`);
  return response.data;
};

export const getPrice = async (symbol: string): Promise<PriceData> => {
  const response = await axios.get(`${API_BASE_URL}/prices/${symbol}`);
  return response.data;
};

export const getChartData = async (symbol: string): Promise<ChartData[]> => {
  const response = await axios.get(`${API_BASE_URL}/chart/${symbol}`);
  return response.data;
}; 