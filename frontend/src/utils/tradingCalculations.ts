interface PositionCalculatorInput {
    accountBalance: number;
    riskPercentage: number;
    entryPrice: number;
    stopLossPrice: number;
    leverage: number;
    marginType: 'ISOLATED' | 'CROSS';
}

interface PositionCalculatorResult {
    positionSize: number;
    quantity: number;
    marginRequired: number;
    liquidationPrice: number;
    riskAmount: number;
    potentialProfit: number;
    potentialLoss: number;
}

export const calculatePositionSize = ({
    accountBalance,
    riskPercentage,
    entryPrice,
    stopLossPrice,
    leverage,
    marginType
}: PositionCalculatorInput): PositionCalculatorResult => {
    // 리스크 금액 계산
    const riskAmount = accountBalance * (riskPercentage / 100);
    
    // 가격 변동폭 계산
    const priceDifference = Math.abs(entryPrice - stopLossPrice);
    const priceChangePercentage = (priceDifference / entryPrice) * 100;
    
    // 포지션 크기 계산
    const positionSize = (riskAmount / (priceChangePercentage / 100)) * leverage;
    
    // 수량 계산
    const quantity = positionSize / entryPrice;
    
    // 필요 증거금 계산
    const marginRequired = positionSize / leverage;
    
    // 청산가 계산
    const liquidationPrice = marginType === 'ISOLATED' 
        ? entryPrice * (1 - (1 / leverage))
        : entryPrice * (1 - (0.5 / leverage));
    
    // 예상 수익/손실 계산
    const potentialProfit = positionSize * (priceChangePercentage / 100);
    const potentialLoss = riskAmount;

    return {
        positionSize,
        quantity,
        marginRequired,
        liquidationPrice,
        riskAmount,
        potentialProfit,
        potentialLoss
    };
};

export const calculateFees = (
    positionSize: number,
    leverage: number,
    isMaker: boolean
): number => {
    const baseFee = isMaker ? 0.02 : 0.04; // 기본 수수료율 (%)
    const leverageFee = leverage > 1 ? (leverage - 1) * 0.01 : 0; // 레버리지 수수료
    
    return positionSize * ((baseFee + leverageFee) / 100);
};

export const calculateLiquidationPrice = (
    entryPrice: number,
    leverage: number,
    marginType: 'ISOLATED' | 'CROSS',
    position: 'LONG' | 'SHORT'
): number => {
    const maintenanceMargin = marginType === 'ISOLATED' ? 0.5 : 0.3; // 유지 증거금 비율 (%)
    const liquidationThreshold = 1 - (maintenanceMargin / leverage);
    
    return position === 'LONG'
        ? entryPrice * liquidationThreshold
        : entryPrice * (2 - liquidationThreshold);
};

export const calculateRiskRewardRatio = (
    entryPrice: number,
    stopLossPrice: number,
    takeProfitPrice: number
): number => {
    const risk = Math.abs(entryPrice - stopLossPrice);
    const reward = Math.abs(takeProfitPrice - entryPrice);
    
    return reward / risk;
};

export const calculateMarginRatio = (
    positionValue: number,
    marginUsed: number,
    unrealizedPnL: number
): number => {
    return ((marginUsed + unrealizedPnL) / positionValue) * 100;
};

export const calculateFundingRate = (
    markPrice: number,
    indexPrice: number,
    timeToFunding: number
): number => {
    const premium = (markPrice - indexPrice) / indexPrice;
    return (premium / timeToFunding) * 100;
}; 