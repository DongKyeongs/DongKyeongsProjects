import React, { useState, useEffect } from 'react';
import { calculatePositionSize, calculateFees, calculateLiquidationPrice } from '../utils/tradingCalculations';
import './PositionCalculator.css';

interface PositionCalculatorProps {
    currentPrice: number;
    accountBalance: number;
}

const PositionCalculator: React.FC<PositionCalculatorProps> = ({ currentPrice, accountBalance }) => {
    const [riskPercentage, setRiskPercentage] = useState<number>(1);
    const [entryPrice, setEntryPrice] = useState<number>(currentPrice);
    const [stopLossPrice, setStopLossPrice] = useState<number>(currentPrice * 0.95);
    const [leverage, setLeverage] = useState<number>(1);
    const [marginType, setMarginType] = useState<'ISOLATED' | 'CROSS'>('CROSS');
    const [position, setPosition] = useState<'LONG' | 'SHORT'>('LONG');
    const [calculation, setCalculation] = useState<any>(null);

    useEffect(() => {
        const result = calculatePositionSize({
            accountBalance,
            riskPercentage,
            entryPrice,
            stopLossPrice,
            leverage,
            marginType
        });

        const fees = calculateFees(result.positionSize, leverage, false);
        const liquidationPrice = calculateLiquidationPrice(
            entryPrice,
            leverage,
            marginType,
            position
        );

        setCalculation({
            ...result,
            fees,
            liquidationPrice
        });
    }, [accountBalance, riskPercentage, entryPrice, stopLossPrice, leverage, marginType, position]);

    return (
        <div className="position-calculator">
            <h3>포지션 계산기</h3>
            
            <div className="calculator-inputs">
                <div className="input-group">
                    <label>계좌 잔고</label>
                    <input
                        type="number"
                        value={accountBalance}
                        readOnly
                    />
                </div>

                <div className="input-group">
                    <label>리스크 비율 (%)</label>
                    <input
                        type="number"
                        value={riskPercentage}
                        onChange={(e) => setRiskPercentage(parseFloat(e.target.value))}
                        min="0.1"
                        max="100"
                        step="0.1"
                    />
                </div>

                <div className="input-group">
                    <label>진입가</label>
                    <input
                        type="number"
                        value={entryPrice}
                        onChange={(e) => setEntryPrice(parseFloat(e.target.value))}
                        min="0"
                        step="0.00000001"
                    />
                </div>

                <div className="input-group">
                    <label>손절가</label>
                    <input
                        type="number"
                        value={stopLossPrice}
                        onChange={(e) => setStopLossPrice(parseFloat(e.target.value))}
                        min="0"
                        step="0.00000001"
                    />
                </div>

                <div className="input-group">
                    <label>레버리지</label>
                    <input
                        type="number"
                        value={leverage}
                        onChange={(e) => setLeverage(parseInt(e.target.value))}
                        min="1"
                        max="125"
                    />
                </div>

                <div className="margin-type-selector">
                    <button
                        className={marginType === 'ISOLATED' ? 'active' : ''}
                        onClick={() => setMarginType('ISOLATED')}
                    >
                        격리
                    </button>
                    <button
                        className={marginType === 'CROSS' ? 'active' : ''}
                        onClick={() => setMarginType('CROSS')}
                    >
                        교차
                    </button>
                </div>

                <div className="position-selector">
                    <button
                        className={`long ${position === 'LONG' ? 'active' : ''}`}
                        onClick={() => setPosition('LONG')}
                    >
                        롱
                    </button>
                    <button
                        className={`short ${position === 'SHORT' ? 'active' : ''}`}
                        onClick={() => setPosition('SHORT')}
                    >
                        숏
                    </button>
                </div>
            </div>

            {calculation && (
                <div className="calculation-results">
                    <div className="result-item">
                        <span>포지션 크기:</span>
                        <span>{calculation.positionSize.toFixed(2)} USDT</span>
                    </div>
                    <div className="result-item">
                        <span>수량:</span>
                        <span>{calculation.quantity.toFixed(8)}</span>
                    </div>
                    <div className="result-item">
                        <span>필요 증거금:</span>
                        <span>{calculation.marginRequired.toFixed(2)} USDT</span>
                    </div>
                    <div className="result-item">
                        <span>청산가:</span>
                        <span>{calculation.liquidationPrice.toFixed(2)} USDT</span>
                    </div>
                    <div className="result-item">
                        <span>예상 수수료:</span>
                        <span>{calculation.fees.toFixed(2)} USDT</span>
                    </div>
                    <div className="result-item">
                        <span>리스크 금액:</span>
                        <span>{calculation.riskAmount.toFixed(2)} USDT</span>
                    </div>
                    <div className="result-item">
                        <span>예상 수익:</span>
                        <span className={position === 'LONG' ? 'profit' : 'loss'}>
                            {calculation.potentialProfit.toFixed(2)} USDT
                        </span>
                    </div>
                    <div className="result-item">
                        <span>예상 손실:</span>
                        <span className="loss">
                            {calculation.potentialLoss.toFixed(2)} USDT
                        </span>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PositionCalculator; 