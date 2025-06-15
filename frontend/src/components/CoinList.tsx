import React, { useEffect, useState } from 'react';
import { getPrices, PriceData } from '../utils/api';
import './CoinList.css';

const CoinList: React.FC = () => {
  const [prices, setPrices] = useState<PriceData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPrices = async () => {
      try {
        const data = await getPrices();
        setPrices(data.filter(price => ['BTCUSDT', 'ETHUSDT'].includes(price.symbol)));
        setLoading(false);
      } catch (err) {
        setError('가격 정보를 불러오는데 실패했습니다.');
        setLoading(false);
      }
    };

    fetchPrices();
    const interval = setInterval(fetchPrices, 10000); // 10초마다 업데이트

    return () => clearInterval(interval);
  }, []);

  if (loading) return <div className="loading">로딩 중...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="coin-list">
      {prices.map((price) => (
        <div key={price.symbol} className="coin-item">
          <div className="coin-name">
            {price.symbol.replace('USDT', '')}
          </div>
          <div className="coin-price">
            ${price.price.toLocaleString()}
          </div>
          <div className={`coin-change ${price.change >= 0 ? 'positive' : 'negative'}`}>
            {price.change >= 0 ? '+' : ''}{price.change.toFixed(2)}%
          </div>
          <div className="coin-volume">
            거래량: {price.volume.toLocaleString()}
          </div>
        </div>
      ))}
    </div>
  );
};

export default CoinList; 