import React, { useState, useEffect } from 'react';
import { portfolioApi } from '../api/portfolio';

interface Portfolio {
    id: number;
    userId: string;
    asset: string;
    balance: number;
    lockedBalance: number;
    averageBuyPrice: number;
    totalInvestment: number;
    lastUpdated: string;
}

const Portfolio: React.FC = () => {
    const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchPortfolios = async () => {
            try {
                const data = await portfolioApi.getUserPortfolios('user1'); // 실제로는 로그인된 사용자 ID 사용
                setPortfolios(data);
                setLoading(false);
            } catch (err) {
                setError('포트폴리오 로딩 실패');
                setLoading(false);
            }
        };

        fetchPortfolios();
    }, []);

    if (loading) return <div className="text-center py-4">로딩중...</div>;
    if (error) return <div className="text-center text-red-500 py-4">{error}</div>;

    return (
        <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-2xl font-bold mb-6">내 포트폴리오</h2>
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">자산</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">보유량</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">평균 매수가</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">총 투자금</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">미실현 손익</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {portfolios.map((portfolio) => (
                            <tr key={portfolio.id}>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm font-medium text-gray-900">{portfolio.asset}</div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-900">{portfolio.balance}</div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-900">${portfolio.averageBuyPrice.toFixed(2)}</div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-900">${portfolio.totalInvestment.toFixed(2)}</div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className={`text-sm ${portfolio.totalInvestment > 0 ? 'text-green-600' : 'text-red-600'}`}>
                                        ${(portfolio.balance * portfolio.averageBuyPrice - portfolio.totalInvestment).toFixed(2)}
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default Portfolio; 