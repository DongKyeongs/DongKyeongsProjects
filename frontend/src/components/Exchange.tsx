import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { chartApi } from '../api/chart';
import { settingsApi } from '../api/settings';
import { connectWebSocket, disconnectWebSocket } from '../utils/websocket';
import TradingChart from './TradingChart';
import Portfolio from './Portfolio';
import NotificationCenter from './NotificationCenter';
import PriceAlertForm from './PriceAlertForm';
import Settings from './Settings';

interface OrderBook {
    bids: [number, number][];
    asks: [number, number][];
}

interface Trade {
    price: number;
    amount: number;
    side: 'buy' | 'sell';
    timestamp: number;
}

const Exchange: React.FC = () => {
    const navigate = useNavigate();
    const [selectedPair, setSelectedPair] = useState('BTC/USDT');
    const [orderBook, setOrderBook] = useState<OrderBook>({ bids: [], asks: [] });
    const [trades, setTrades] = useState<Trade[]>([]);
    const [orderType, setOrderType] = useState<'limit' | 'market'>('limit');
    const [orderSide, setOrderSide] = useState<'buy' | 'sell'>('buy');
    const [price, setPrice] = useState('');
    const [amount, setAmount] = useState('');
    const [chartData, setChartData] = useState<any[]>([]);
    const [selectedInterval, setSelectedInterval] = useState('1h');
    const [currentPrice, setCurrentPrice] = useState(0);
    const [showSettings, setShowSettings] = useState(false);
    const [fee, setFee] = useState(0);

    useEffect(() => {
        const fetchChartData = async () => {
            try {
                const data = await chartApi.getChartData(selectedPair, selectedInterval);
                setChartData(data.map(item => ({
                    time: item.time,
                    open: item.open,
                    high: item.high,
                    low: item.low,
                    close: item.close
                })));
            } catch (error) {
                console.error('차트 데이터 로딩 실패:', error);
            }
        };

        fetchChartData();
    }, [selectedPair, selectedInterval]);

    useEffect(() => {
        connectWebSocket((data) => {
            if (data.bids && data.asks) {
                setOrderBook(data);
                // 현재 가격 업데이트 (매수/매도 호가의 중간값)
                const bestBid = data.bids[0]?.price || 0;
                const bestAsk = data.asks[0]?.price || 0;
                setCurrentPrice((bestBid + bestAsk) / 2);
            } else if (data.price && data.amount) {
                setTrades(prev => [data, ...prev].slice(0, 50));
            }
        });

        return () => {
            disconnectWebSocket();
        };
    }, [selectedPair]);

    useEffect(() => {
        const calculateFee = async () => {
            if (amount && price) {
                const totalAmount = parseFloat(amount) * parseFloat(price);
                const calculatedFee = await settingsApi.calculateFee('user1', totalAmount, orderType === 'LIMIT');
                setFee(calculatedFee);
            }
        };

        calculateFee();
    }, [amount, price, orderType]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            // 주문 API 호출
            // await orderApi.createOrder({
            //     pair: selectedPair,
            //     type: orderType,
            //     side: orderSide,
            //     price: parseFloat(price),
            //     amount: parseFloat(amount)
            // });
            setPrice('');
            setAmount('');
        } catch (error) {
            console.error('주문 실패:', error);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16">
                        <div className="flex">
                            <div className="flex-shrink-0 flex items-center">
                                <h1 className="text-xl font-bold text-gray-900">거래소</h1>
                            </div>
                        </div>
                        <div className="flex items-center space-x-4">
                            <NotificationCenter />
                            <button
                                onClick={() => setShowSettings(true)}
                                className="px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                            >
                                설정
                            </button>
                            <button
                                onClick={() => navigate('/dashboard')}
                                className="px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
                            >
                                대시보드
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
                <div className="px-4 py-6 sm:px-0">
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        {/* 차트 영역 */}
                        <div className="lg:col-span-2 space-y-6">
                            <div className="bg-white shadow rounded-lg p-6">
                                <div className="flex justify-between items-center mb-4">
                                    <h2 className="text-xl font-bold text-gray-900">{selectedPair}</h2>
                                    <div className="flex space-x-2">
                                        {['1m', '5m', '15m', '30m', '1h', '4h', '1d'].map((interval) => (
                                            <button
                                                key={interval}
                                                onClick={() => setSelectedInterval(interval)}
                                                className={`px-3 py-1 rounded ${
                                                    selectedInterval === interval
                                                        ? 'bg-blue-600 text-white'
                                                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                                }`}
                                            >
                                                {interval}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                                <TradingChart data={chartData} pair={selectedPair} />
                            </div>

                            {/* 가격 알림 설정 */}
                            <PriceAlertForm pair={selectedPair} currentPrice={currentPrice} />
                        </div>

                        {/* 주문서 및 거래 폼 */}
                        <div className="space-y-6">
                            <div className="bg-white shadow rounded-lg p-6">
                                <h3 className="text-lg font-medium text-gray-900 mb-4">주문서</h3>
                                <div className="space-y-4">
                                    <div>
                                        <h4 className="text-sm font-medium text-gray-500 mb-2">매수 호가</h4>
                                        <div className="space-y-1">
                                            {orderBook.bids.slice(0, 5).map((bid, index) => (
                                                <div key={index} className="flex justify-between text-sm">
                                                    <span className="text-green-600">{bid.price}</span>
                                                    <span className="text-gray-600">{bid.amount}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                    <div>
                                        <h4 className="text-sm font-medium text-gray-500 mb-2">매도 호가</h4>
                                        <div className="space-y-1">
                                            {orderBook.asks.slice(0, 5).map((ask, index) => (
                                                <div key={index} className="flex justify-between text-sm">
                                                    <span className="text-red-600">{ask.price}</span>
                                                    <span className="text-gray-600">{ask.amount}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-white shadow rounded-lg p-6">
                                <h3 className="text-lg font-medium text-gray-900 mb-4">거래</h3>
                                <div className="space-y-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700">주문 유형</label>
                                        <select
                                            value={orderType}
                                            onChange={(e) => setOrderType(e.target.value)}
                                            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
                                        >
                                            <option value="MARKET">시장가</option>
                                            <option value="LIMIT">지정가</option>
                                            <option value="STOP_LOSS">스탑로스</option>
                                            <option value="TAKE_PROFIT">익절</option>
                                        </select>
                                    </div>

                                    {orderType !== 'MARKET' && (
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700">가격</label>
                                            <input
                                                type="number"
                                                value={price}
                                                onChange={(e) => setPrice(e.target.value)}
                                                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                            />
                                        </div>
                                    )}

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700">수량</label>
                                        <input
                                            type="number"
                                            value={amount}
                                            onChange={(e) => setAmount(e.target.value)}
                                            className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                        />
                                    </div>

                                    <div className="flex space-x-4">
                                        <button
                                            onClick={() => handleOrder('BUY')}
                                            className="flex-1 bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                                        >
                                            매수
                                        </button>
                                        <button
                                            onClick={() => handleOrder('SELL')}
                                            className="flex-1 bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                                        >
                                            매도
                                        </button>
                                    </div>
                                </div>
                            </div>

                            {/* 포트폴리오 */}
                            <Portfolio />
                        </div>
                    </div>

                    {/* 거래 내역 */}
                    <div className="mt-6 bg-white shadow rounded-lg p-6">
                        <h3 className="text-lg font-medium text-gray-900 mb-4">거래 내역</h3>
                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">시간</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가격</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">수량</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">거래 유형</th>
                                    </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                    {trades.map((trade, index) => (
                                        <tr key={index}>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                                {new Date(trade.timestamp).toLocaleString()}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                                ${trade.price}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                                {trade.amount}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                                    trade.side === 'BUY' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                                }`}>
                                                    {trade.side}
                                                </span>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </main>

            {/* 설정 모달 */}
            {showSettings && (
                <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4">
                    <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="p-6">
                            <div className="flex justify-between items-center mb-6">
                                <h2 className="text-2xl font-bold text-gray-900">거래소 설정</h2>
                                <button
                                    onClick={() => setShowSettings(false)}
                                    className="text-gray-400 hover:text-gray-500"
                                >
                                    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </div>
                            <Settings />
                        </div>
                    </div>
                </div>
            )}

            {/* 수수료 표시 */}
            {fee > 0 && (
                <div className="fixed bottom-4 right-4 bg-white shadow-lg rounded-lg p-4">
                    <div className="text-sm text-gray-500">예상 수수료</div>
                    <div className="text-lg font-semibold text-gray-900">${fee.toFixed(2)}</div>
                </div>
            )}
        </div>
    );
};

export default Exchange; 