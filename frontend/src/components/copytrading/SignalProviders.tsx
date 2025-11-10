import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './SignalProviders.css';

interface SignalProvider {
  id: number;
  nickname: string;
  description: string;
  strategy: string;
  totalReturnPercent: string;
  monthlyReturnPercent: string;
  winRate: string;
  totalTrades: number;
  followerCount: number;
  performanceFeePercent: string;
  minCopyAmount: string;
  maxCopyAmount: string;
  ranking: number;
}

const SignalProviders: React.FC = () => {
  const navigate = useNavigate();
  const [providers, setProviders] = useState<SignalProvider[]>([]);
  const [filter, setFilter] = useState<'all' | 'top' | 'popular'>('all');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProviders();
  }, [filter]);

  const loadProviders = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      let url = '/api/copy-trading/providers';
      if (filter === 'top') url = '/api/copy-trading/providers/top?limit=20';
      if (filter === 'popular') url = '/api/copy-trading/providers/popular?limit=20';

      const response = await axios.get<SignalProvider[]>(url, config);
      setProviders(response.data);
    } catch (error) {
      console.error('Failed to load providers:', error);
    } finally {
      setLoading(false);
    }
  };

  const viewDetails = (providerId: number) => {
    navigate(`/copy-trading/provider/${providerId}`);
  };

  const startCopy = (provider: SignalProvider) => {
    // TODO: 복사 설정 모달 열기
    alert(`Starting copy trade with ${provider.nickname}`);
  };

  return (
    <div className="signal-providers">
      <div className="header">
        <h1>Signal Providers</h1>
        <div className="filters">
          <button
            className={filter === 'all' ? 'active' : ''}
            onClick={() => setFilter('all')}
          >
            All Providers
          </button>
          <button
            className={filter === 'top' ? 'active' : ''}
            onClick={() => setFilter('top')}
          >
            Top Performers
          </button>
          <button
            className={filter === 'popular' ? 'active' : ''}
            onClick={() => setFilter('popular')}
          >
            Most Followed
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading">Loading providers...</div>
      ) : (
        <div className="providers-grid">
          {providers.map((provider) => (
            <div key={provider.id} className="provider-card">
              <div className="provider-header">
                <div className="rank">#{provider.ranking}</div>
                <h3>{provider.nickname}</h3>
              </div>

              <div className="provider-stats">
                <div className="stat">
                  <span className="label">Total Return</span>
                  <span className={`value ${parseFloat(provider.totalReturnPercent) >= 0 ? 'positive' : 'negative'}`}>
                    {parseFloat(provider.totalReturnPercent).toFixed(2)}%
                  </span>
                </div>
                <div className="stat">
                  <span className="label">Monthly Return</span>
                  <span className={`value ${parseFloat(provider.monthlyReturnPercent) >= 0 ? 'positive' : 'negative'}`}>
                    {parseFloat(provider.monthlyReturnPercent).toFixed(2)}%
                  </span>
                </div>
                <div className="stat">
                  <span className="label">Win Rate</span>
                  <span className="value">{parseFloat(provider.winRate).toFixed(2)}%</span>
                </div>
                <div className="stat">
                  <span className="label">Followers</span>
                  <span className="value">{provider.followerCount}</span>
                </div>
              </div>

              <div className="provider-info">
                <p className="strategy">{provider.strategy || 'No strategy description'}</p>
                <div className="copy-info">
                  <span>Fee: {parseFloat(provider.performanceFeePercent).toFixed(1)}%</span>
                  <span>Min: ${parseFloat(provider.minCopyAmount).toFixed(0)}</span>
                </div>
              </div>

              <div className="provider-actions">
                <button className="btn-view" onClick={() => viewDetails(provider.id)}>
                  View Details
                </button>
                <button className="btn-copy" onClick={() => startCopy(provider)}>
                  Start Copy
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default SignalProviders;
