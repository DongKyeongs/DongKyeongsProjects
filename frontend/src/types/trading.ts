export enum TradingType {
    SPOT = 'SPOT',      // 현물거래
    FUTURES = 'FUTURES' // 선물거래
}

export interface TradingPair {
    symbol: string;
    baseAsset: string;
    quoteAsset: string;
    tradingType: TradingType;
    minQty: number;
    maxQty: number;
    priceDecimals: number;
    quantityDecimals: number;
}

export interface FuturesContract {
    symbol: string;
    baseAsset: string;
    quoteAsset: string;
    expiryDate: Date;
    leverage: number;
    marginType: 'ISOLATED' | 'CROSS';
    maintenanceMargin: number;
    initialMargin: number;
}

export interface SpotOrder {
    symbol: string;
    side: 'BUY' | 'SELL';
    type: 'LIMIT' | 'MARKET';
    quantity: number;
    price?: number;
    tradingType: TradingType.SPOT;
}

export interface FuturesOrder {
    symbol: string;
    side: 'BUY' | 'SELL';
    type: 'LIMIT' | 'MARKET';
    quantity: number;
    price?: number;
    tradingType: TradingType.FUTURES;
    leverage: number;
    marginType: 'ISOLATED' | 'CROSS';
    stopLoss?: number;
    takeProfit?: number;
} 