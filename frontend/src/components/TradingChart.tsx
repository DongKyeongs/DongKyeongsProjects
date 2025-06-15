import React, { useEffect, useRef } from 'react';
import { createChart, ColorType, IChartApi } from 'lightweight-charts';

interface TradingChartProps {
    data: {
        time: string;
        open: number;
        high: number;
        low: number;
        close: number;
    }[];
    pair: string;
}

const TradingChart: React.FC<TradingChartProps> = ({ data, pair }) => {
    const chartContainerRef = useRef<HTMLDivElement>(null);
    const chartRef = useRef<IChartApi | null>(null);

    useEffect(() => {
        if (chartContainerRef.current) {
            const chart = createChart(chartContainerRef.current, {
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

            const candlestickSeries = chart.addCandlestickSeries({
                upColor: '#26a69a',
                downColor: '#ef5350',
                borderVisible: false,
                wickUpColor: '#26a69a',
                wickDownColor: '#ef5350',
            });

            candlestickSeries.setData(data);

            chart.timeScale().fitContent();

            chartRef.current = chart;

            const handleResize = () => {
                if (chartContainerRef.current) {
                    chart.applyOptions({
                        width: chartContainerRef.current.clientWidth,
                    });
                }
            };

            window.addEventListener('resize', handleResize);

            return () => {
                window.removeEventListener('resize', handleResize);
                chart.remove();
            };
        }
    }, [data]);

    return (
        <div className="relative">
            <div className="absolute top-4 left-4 z-10">
                <h2 className="text-xl font-bold text-gray-900">{pair}</h2>
            </div>
            <div ref={chartContainerRef} className="w-full" />
        </div>
    );
};

export default TradingChart; 