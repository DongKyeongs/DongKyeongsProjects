import React, { useState } from 'react';
import { TradingType, SpotOrder, FuturesOrder, TradingPair } from '../types/trading';
import './TradingPanel.css';

interface TradingPanelProps {
    tradingPair: TradingPair;
    onOrderSubmit: (order: SpotOrder | FuturesOrder) => void;
}

const TradingPanel: React.FC<TradingPanelProps> = ({ tradingPair, onOrderSubmit }) => {
    const [orderType, setOrderType] = useState<'LIMIT' | 'MARKET'>('LIMIT');
    const [side, setSide] = useState<'BUY' | 'SELL'>('BUY');
    const [quantity, setQuantity] = useState<string>('');
    const [price, setPrice] = useState<string>('');
    const [leverage, setLeverage] = useState<number>(1);
    const [marginType, setMarginType] = useState<'ISOLATED' | 'CROSS'>('CROSS');
    const [stopLoss, setStopLoss] = useState<string>('');
    const [takeProfit, setTakeProfit] = useState<string>('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        
        const baseOrder = {
            symbol: tradingPair.symbol,
            side,
            type: orderType,
            quantity: parseFloat(quantity),
            price: orderType === 'LIMIT' ? parseFloat(price) : undefined,
        };

        if (tradingPair.tradingType === TradingType.SPOT) {
            onOrderSubmit({
                ...baseOrder,
                tradingType: TradingType.SPOT
            } as SpotOrder);
        } else {
            onOrderSubmit({
                ...baseOrder,
                tradingType: TradingType.FUTURES,
                leverage,
                marginType,
                stopLoss: stopLoss ? parseFloat(stopLoss) : undefined,
                takeProfit: takeProfit ? parseFloat(takeProfit) : undefined
            } as FuturesOrder);
        }
    };

    return (
        <div className="trading-panel">
            <div className="trading-type-selector">
                <button 
                    className={tradingPair.tradingType === TradingType.SPOT ? 'active' : ''}
                    onClick={() => {/* 현물거래로 전환 */}}
                >
                    현물거래
                </button>
                <button 
                    className={tradingPair.tradingType === TradingType.FUTURES ? 'active' : ''}
                    onClick={() => {/* 선물거래로 전환 */}}
                >
                    선물거래
                </button>
            </div>

            <form onSubmit={handleSubmit}>
                <div className="order-type-selector">
                    <button 
                        type="button"
                        className={orderType === 'LIMIT' ? 'active' : ''}
                        onClick={() => setOrderType('LIMIT')}
                    >
                        지정가
                    </button>
                    <button 
                        type="button"
                        className={orderType === 'MARKET' ? 'active' : ''}
                        onClick={() => setOrderType('MARKET')}
                    >
                        시장가
                    </button>
                </div>

                <div className="side-selector">
                    <button 
                        type="button"
                        className={`buy ${side === 'BUY' ? 'active' : ''}`}
                        onClick={() => setSide('BUY')}
                    >
                        매수
                    </button>
                    <button 
                        type="button"
                        className={`sell ${side === 'SELL' ? 'active' : ''}`}
                        onClick={() => setSide('SELL')}
                    >
                        매도
                    </button>
                </div>

                <div className="input-group">
                    <label>수량</label>
                    <input
                        type="number"
                        value={quantity}
                        onChange={(e) => setQuantity(e.target.value)}
                        step={Math.pow(10, -tradingPair.quantityDecimals)}
                        min={tradingPair.minQty}
                        max={tradingPair.maxQty}
                        required
                    />
                </div>

                {orderType === 'LIMIT' && (
                    <div className="input-group">
                        <label>가격</label>
                        <input
                            type="number"
                            value={price}
                            onChange={(e) => setPrice(e.target.value)}
                            step={Math.pow(10, -tradingPair.priceDecimals)}
                            required
                        />
                    </div>
                )}

                {tradingPair.tradingType === TradingType.FUTURES && (
                    <>
                        <div className="input-group">
                            <label>레버리지</label>
                            <input
                                type="number"
                                value={leverage}
                                onChange={(e) => setLeverage(parseInt(e.target.value))}
                                min={1}
                                max={125}
                                required
                            />
                        </div>

                        <div className="margin-type-selector">
                            <button 
                                type="button"
                                className={marginType === 'ISOLATED' ? 'active' : ''}
                                onClick={() => setMarginType('ISOLATED')}
                            >
                                격리
                            </button>
                            <button 
                                type="button"
                                className={marginType === 'CROSS' ? 'active' : ''}
                                onClick={() => setMarginType('CROSS')}
                            >
                                교차
                            </button>
                        </div>

                        <div className="input-group">
                            <label>손절가</label>
                            <input
                                type="number"
                                value={stopLoss}
                                onChange={(e) => setStopLoss(e.target.value)}
                                step={Math.pow(10, -tradingPair.priceDecimals)}
                            />
                        </div>

                        <div className="input-group">
                            <label>익절가</label>
                            <input
                                type="number"
                                value={takeProfit}
                                onChange={(e) => setTakeProfit(e.target.value)}
                                step={Math.pow(10, -tradingPair.priceDecimals)}
                            />
                        </div>
                    </>
                )}

                <button type="submit" className={`submit-button ${side.toLowerCase()}`}>
                    {side === 'BUY' ? '매수하기' : '매도하기'}
                </button>
            </form>
        </div>
    );
};

export default TradingPanel; 