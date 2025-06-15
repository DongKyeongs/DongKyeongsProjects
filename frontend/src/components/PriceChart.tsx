import React, { useEffect, useRef, useState } from 'react';
import { createChart, ColorType, IChartApi, ISeriesApi, CandlestickData, Time, SeriesType, SeriesOptionsMap } from 'lightweight-charts';
import { websocketService } from '../services/websocket';
import { calculateMA, calculateRSI, calculateMACD, calculateBollingerBands } from '../utils/indicators';
import './PriceChart.css';

interface PriceChartProps {
    symbol: string;
    interval: '1m' | '5m' | '15m' | '1h' | '4h' | '1d';
}

const PriceChart: React.FC<PriceChartProps> = ({ symbol, interval }) => {
    const chartContainerRef = useRef<HTMLDivElement>(null);
    const [chart, setChart] = useState<IChartApi | null>(null);
    const [candleSeries, setCandleSeries] = useState<ISeriesApi<"Candlestick"> | null>(null);
    const [volumeSeries, setVolumeSeries] = useState<ISeriesApi<"Histogram"> | null>(null);
    const [candles, setCandles] = useState<CandlestickData[]>([]);
    const [showIndicators, setShowIndicators] = useState({
        ma: true,
        rsi: false,
        macd: false,
        bollinger: false
    });

    useEffect(() => {
        if (chartContainerRef.current) {
            const newChart = createChart(chartContainerRef.current, {
                layout: {
                    background: { type: ColorType.Solid, color: '#ffffff' },
                    textColor: '#333',
                },
                grid: {
                    vertLines: { color: '#f0f0f0' },
                    horzLines: { color: '#f0f0f0' },
                },
                width: chartContainerRef.current.clientWidth,
                height: 400,
            });

            // 캔들스틱 시리즈 생성
            const newCandleSeries = newChart.addSeries({
                type: 'Candlestick',
                isBuiltIn: true,
                defaultOptions: {
                    wickVisible: true,
                    borderVisible: false,
                    borderColor: '#26a69a',
                    borderUpColor: '#26a69a',
                    borderDownColor: '#ef5350',
                    wickColor: '#26a69a',
                    wickUpColor: '#26a69a',
                    wickDownColor: '#ef5350',
                    upColor: '#26a69a',
                    downColor: '#ef5350',
                }
            });

            // 거래량 시리즈 생성
            const newVolumeSeries = newChart.addSeries({
                type: 'Histogram',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#26a69a',
                    base: 0,
                }
            });

            setChart(newChart);
            setCandleSeries(newCandleSeries);
            setVolumeSeries(newVolumeSeries);

            // 초기 데이터 로드
            fetch(`http://localhost:8080/api/coins/${symbol}/candles?interval=${interval}`)
                .then(response => response.json())
                .then(data => {
                    setCandles(data);
                    newCandleSeries.setData(data);
                    newVolumeSeries.setData(data.map(candle => ({
                        time: candle.time,
                        value: candle.volume,
                        color: candle.close >= candle.open ? '#26a69a' : '#ef5350'
                    })));
                });

            // WebSocket 구독
            websocketService.subscribeToCoin(symbol, (update) => {
                const lastCandle = candles[candles.length - 1];
                const currentTime = new Date(update.timestamp).getTime() / 1000;

                if (lastCandle && currentTime - (lastCandle.time as number) < getIntervalInSeconds(interval)) {
                    // 현재 캔들 업데이트
                    const updatedCandle = {
                        ...lastCandle,
                        close: update.price,
                        high: Math.max(lastCandle.high, update.price),
                        low: Math.min(lastCandle.low, update.price),
                    };
                    const newCandles = [...candles.slice(0, -1), updatedCandle];
                    setCandles(newCandles);
                    newCandleSeries.update(updatedCandle);
                    newVolumeSeries.update({
                        time: currentTime as Time,
                        value: update.volume24h,
                        color: updatedCandle.close >= updatedCandle.open ? '#26a69a' : '#ef5350'
                    });
                } else {
                    // 새로운 캔들 생성
                    const newCandle: CandlestickData = {
                        time: currentTime as Time,
                        open: update.price,
                        high: update.price,
                        low: update.price,
                        close: update.price,
                    };
                    const newCandles = [...candles, newCandle];
                    setCandles(newCandles);
                    newCandleSeries.update(newCandle);
                    newVolumeSeries.update({
                        time: currentTime as Time,
                        value: update.volume24h,
                        color: newCandle.close >= newCandle.open ? '#26a69a' : '#ef5350'
                    });
                }
            });

            return () => {
                newChart.remove();
                websocketService.unsubscribeFromCoin(symbol);
            };
        }
    }, [symbol, interval]);

    // 기술적 지표 업데이트
    useEffect(() => {
        if (!chart || !candleSeries || candles.length === 0) return;

        // 이동평균선
        if (showIndicators.ma) {
            const ma20 = calculateMA(candles, 20);
            const ma50 = calculateMA(candles, 50);
            const ma200 = calculateMA(candles, 200);

            const ma20Series = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#2196F3',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#2196F3',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });
            const ma50Series = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#FF9800',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#FF9800',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });
            const ma200Series = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#9C27B0',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#9C27B0',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });

            ma20Series.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: ma20[i]
            })));
            ma50Series.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: ma50[i]
            })));
            ma200Series.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: ma200[i]
            })));
        }

        // RSI
        if (showIndicators.rsi) {
            const rsi = calculateRSI(candles);
            const rsiSeries = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#4CAF50',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#4CAF50',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });
            rsiSeries.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: rsi[i]
            })));
        }

        // MACD
        if (showIndicators.macd) {
            const { macd, signal, histogram } = calculateMACD(candles);
            const macdSeries = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#2196F3',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#2196F3',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });
            const signalSeries = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#FF9800',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#FF9800',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });
            const histogramSeries = chart.addSeries({
                type: 'Histogram',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#4CAF50',
                    base: 0,
                }
            });

            macdSeries.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: macd[i]
            })));
            signalSeries.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: signal[i]
            })));
            histogramSeries.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: histogram[i],
                color: histogram[i] >= 0 ? '#4CAF50' : '#F44336'
            })));
        }

        // 볼린저 밴드
        if (showIndicators.bollinger) {
            const { upper, middle, lower } = calculateBollingerBands(candles);
            const upperSeries = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#2196F3',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#2196F3',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });
            const middleSeries = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#FF9800',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#FF9800',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });
            const lowerSeries = chart.addSeries({
                type: 'Line',
                isBuiltIn: true,
                defaultOptions: {
                    color: '#2196F3',
                    lineWidth: 2,
                    lineStyle: 0,
                    lineType: 0,
                    lineVisible: true,
                    pointMarkersVisible: false,
                    crosshairMarkerVisible: true,
                    crosshairMarkerRadius: 4,
                    crosshairMarkerBorderColor: '#2196F3',
                    crosshairMarkerBackgroundColor: '#ffffff',
                    crosshairMarkerBorderWidth: 1,
                    lastPriceAnimation: 0
                }
            });

            upperSeries.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: upper[i]
            })));
            middleSeries.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: middle[i]
            })));
            lowerSeries.setData(candles.map((candle, i) => ({
                time: candle.time,
                value: lower[i]
            })));
        }
    }, [chart, candleSeries, candles, showIndicators]);

    const getIntervalInSeconds = (interval: string): number => {
        switch (interval) {
            case '1m': return 60;
            case '5m': return 300;
            case '15m': return 900;
            case '1h': return 3600;
            case '4h': return 14400;
            case '1d': return 86400;
            default: return 60;
        }
    };

    return (
        <div className="price-chart">
            <div className="chart-header">
                <h3>{symbol} 차트</h3>
                <div className="chart-controls">
                    <div className="interval-selector">
                        {['1m', '5m', '15m', '1h', '4h', '1d'].map((i) => (
                            <button
                                key={i}
                                className={interval === i ? 'active' : ''}
                                onClick={() => {/* interval 변경 로직 */}}
                            >
                                {i}
                            </button>
                        ))}
                    </div>
                    <div className="indicator-selector">
                        <button
                            className={showIndicators.ma ? 'active' : ''}
                            onClick={() => setShowIndicators(prev => ({ ...prev, ma: !prev.ma }))}
                        >
                            MA
                        </button>
                        <button
                            className={showIndicators.rsi ? 'active' : ''}
                            onClick={() => setShowIndicators(prev => ({ ...prev, rsi: !prev.rsi }))}
                        >
                            RSI
                        </button>
                        <button
                            className={showIndicators.macd ? 'active' : ''}
                            onClick={() => setShowIndicators(prev => ({ ...prev, macd: !prev.macd }))}
                        >
                            MACD
                        </button>
                        <button
                            className={showIndicators.bollinger ? 'active' : ''}
                            onClick={() => setShowIndicators(prev => ({ ...prev, bollinger: !prev.bollinger }))}
                        >
                            BB
                        </button>
                    </div>
                </div>
            </div>
            <div ref={chartContainerRef} className="chart-container" />
        </div>
    );
};

export default PriceChart; 