export interface PriceUpdate {
    symbol: string;
    price: number;
    volume24h: number;
    priceChange24h: number;
    timestamp: string;
}

export interface Coin {
    id: number;
    symbol: string;
    name: string;
    currentPrice: number;
    volume24h: number;
    marketCap: number;
    isActive: boolean;
} 