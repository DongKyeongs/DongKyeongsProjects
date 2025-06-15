import React, { useState, useEffect } from 'react';
import { IChartApi, ISeriesApi, LineStyle, Time } from 'lightweight-charts';
import './ChartTools.css';

interface ChartToolsProps {
    chart: IChartApi | null;
    onAlertCreate: (price: number, condition: 'above' | 'below') => void;
    onExportImage: () => void;
    onExportCSV: () => void;
}

interface TrendLine {
    id: string;
    startPrice: number;
    endPrice: number;
    startTime: Time;
    endTime: Time;
}

interface FibonacciLevel {
    id: string;
    startPrice: number;
    endPrice: number;
    startTime: Time;
    endTime: Time;
}

const ChartTools: React.FC<ChartToolsProps> = ({
    chart,
    onAlertCreate,
    onExportImage,
    onExportCSV
}) => {
    const [activeTool, setActiveTool] = useState<'trendline' | 'fibonacci' | 'alert' | null>(null);
    const [trendLines, setTrendLines] = useState<TrendLine[]>([]);
    const [fibonacciLevels, setFibonacciLevels] = useState<FibonacciLevel[]>([]);
    const [isDrawing, setIsDrawing] = useState(false);
    const [startPoint, setStartPoint] = useState<{ price: number; time: Time } | null>(null);
    const [alertPrice, setAlertPrice] = useState<string>('');
    const [lineSeries, setLineSeries] = useState<ISeriesApi<'Line'> | null>(null);

    useEffect(() => {
        if (chart && !lineSeries) {
            // @ts-expect-error
            const series = chart.addSeries({ type: 'Line' });
            setLineSeries(series);
        }
    }, [chart, lineSeries]);

    const handleMouseDown = (e: React.MouseEvent) => {
        if (!chart || !activeTool || !lineSeries) return;

        const rect = chart.chartElement().getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const price = lineSeries.coordinateToPrice(y);
        const time = chart.timeScale().coordinateToTime(x);

        if (price !== null && time !== null) {
            setIsDrawing(true);
            setStartPoint({ price, time });
        }
    };

    const handleMouseMove = (e: React.MouseEvent) => {
        if (!chart || !isDrawing || !startPoint || !lineSeries) return;

        const rect = chart.chartElement().getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const price = lineSeries.coordinateToPrice(y);
        const time = chart.timeScale().coordinateToTime(x);

        if (price !== null && time !== null) {
            // 임시 라인 그리기
            if (activeTool === 'trendline') {
                // @ts-expect-error
                const tempLine = chart.addSeries({ type: 'Line' });
                tempLine.applyOptions({ color: '#2196F3', lineWidth: 2, lineStyle: LineStyle.Dashed });
                tempLine.setData([
                    { time: startPoint.time, value: startPoint.price },
                    { time, value: price }
                ]);
            }
        }
    };

    const handleMouseUp = (e: React.MouseEvent) => {
        if (!chart || !isDrawing || !startPoint || !lineSeries) return;

        const rect = chart.chartElement().getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const price = lineSeries.coordinateToPrice(y);
        const time = chart.timeScale().coordinateToTime(x);

        if (price !== null && time !== null) {
            if (activeTool === 'trendline') {
                const newTrendLine: TrendLine = {
                    id: Date.now().toString(),
                    startPrice: startPoint.price,
                    endPrice: price,
                    startTime: startPoint.time,
                    endTime: time
                };
                setTrendLines([...trendLines, newTrendLine]);
            } else if (activeTool === 'fibonacci') {
                const newFibonacci: FibonacciLevel = {
                    id: Date.now().toString(),
                    startPrice: startPoint.price,
                    endPrice: price,
                    startTime: startPoint.time,
                    endTime: time
                };
                setFibonacciLevels([...fibonacciLevels, newFibonacci]);
                drawFibonacciLevels(newFibonacci);
            }
        }

        setIsDrawing(false);
        setStartPoint(null);
    };

    const drawFibonacciLevels = (fib: FibonacciLevel) => {
        if (!chart) return;

        const levels = [0, 0.236, 0.382, 0.5, 0.618, 0.786, 1];
        const priceDiff = fib.endPrice - fib.startPrice;

        levels.forEach(level => {
            const price = fib.startPrice + (priceDiff * level);
            // @ts-expect-error
            const line = chart.addSeries({ type: 'Line' });
            line.applyOptions({ color: '#FF9800', lineWidth: 1, lineStyle: LineStyle.Dashed });
            line.setData([
                { time: fib.startTime, value: price },
                { time: fib.endTime, value: price }
            ]);
        });
    };

    const handleAlertSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const price = parseFloat(alertPrice);
        if (!isNaN(price)) {
            onAlertCreate(price, price > 0 ? 'above' : 'below');
            setAlertPrice('');
        }
    };

    return (
        <div className="chart-tools">
            <div className="tools-buttons">
                <button
                    className={activeTool === 'trendline' ? 'active' : ''}
                    onClick={() => setActiveTool(activeTool === 'trendline' ? null : 'trendline')}
                >
                    트렌드라인
                </button>
                <button
                    className={activeTool === 'fibonacci' ? 'active' : ''}
                    onClick={() => setActiveTool(activeTool === 'fibonacci' ? null : 'fibonacci')}
                >
                    피보나치
                </button>
                <button
                    className={activeTool === 'alert' ? 'active' : ''}
                    onClick={() => setActiveTool(activeTool === 'alert' ? null : 'alert')}
                >
                    알림
                </button>
                <button onClick={onExportImage}>
                    이미지 저장
                </button>
                <button onClick={onExportCSV}>
                    CSV 내보내기
                </button>
            </div>

            {activeTool === 'alert' && (
                <form onSubmit={handleAlertSubmit} className="alert-form">
                    <input
                        type="number"
                        value={alertPrice}
                        onChange={(e) => setAlertPrice(e.target.value)}
                        placeholder="가격 입력"
                        step="0.01"
                    />
                    <button type="submit">알림 설정</button>
                </form>
            )}

            <div
                className="chart-area"
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
            />
        </div>
    );
};

export default ChartTools; 