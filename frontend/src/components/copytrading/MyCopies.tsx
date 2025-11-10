import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './MyCopies.css';

interface CopyTrade {
  id: number;
  provider: { id: number; nickname: string };
  status: string;
  mode: string;
  copyAmount: string;
  copyRatio: string;
  totalProfit: string;
  netProfit: string;
  returnPercent: string;
  copiedTrades: number;
  successfulTrades: number;
  failedTrades: number;
  startedAt: string;
  lastCopiedAt: string | null;
}

const MyCopies: React.FC = () => {
  const [copies, setCopies] = useState<CopyTrade[]>([]);
  const [loading, setLoading] = useState(true);
  const userId = 1; // TODO: Get from auth context

  useEffect(() => {
    loadMyCopies();
  }, []);

  const loadMyCopies = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const response = await axios.get<CopyTrade[]>(
        `/api/copy-trading/my-copies?followerId=${userId}`,
        config
      );

      setCopies(response.data);
    } catch (error) {
      console.error('Failed to load my copies:', error);
    } finally {
      setLoading(false);
    }
  };

  const stopCopy = async (copyId: number) => {
    if (!window.confirm('Are you sure you want to stop this copy trade?')) {
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(`/api/copy-trading/${copyId}/stop`, {}, config);
      alert('Copy trade stopped successfully');
      loadMyCopies();
    } catch (error) {
      console.error('Failed to stop copy trade:', error);
      alert('Failed to stop copy trade');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'status-active';
      case 'PAUSED':
        return 'status-paused';
      case 'STOPPED':
        return 'status-stopped';
      default:
        return '';
    }
  };

  return (
    <div className="my-copies">
      <h1>My Copy Trades</h1>

      {loading ? (
        <div className="loading">Loading your copies...</div>
      ) : copies.length === 0 ? (
        <div className="empty">
          <p>You are not copying any traders yet</p>
          <button onClick={() => window.location.href = '/copy-trading'}>
            Browse Signal Providers
          </button>
        </div>
      ) : (
        <div className="copies-list">
          {copies.map((copy) => (
            <div key={copy.id} className="copy-card">
              <div className="copy-header">
                <div>
                  <h3>{copy.provider.nickname}</h3>
                  <span className={`status ${getStatusColor(copy.status)}`}>
                    {copy.status}
                  </span>
                </div>
                <div className="mode-badge">{copy.mode}</div>
              </div>

              <div className="copy-stats">
                <div className="stat">
                  <span className="label">Net Profit</span>
                  <span className={`value ${parseFloat(copy.netProfit) >= 0 ? 'positive' : 'negative'}`}>
                    ${parseFloat(copy.netProfit).toFixed(2)}
                  </span>
                </div>
                <div className="stat">
                  <span className="label">Return</span>
                  <span className={`value ${parseFloat(copy.returnPercent) >= 0 ? 'positive' : 'negative'}`}>
                    {parseFloat(copy.returnPercent).toFixed(2)}%
                  </span>
                </div>
                <div className="stat">
                  <span className="label">Copied Trades</span>
                  <span className="value">{copy.copiedTrades}</span>
                </div>
                <div className="stat">
                  <span className="label">Win Rate</span>
                  <span className="value">
                    {copy.copiedTrades > 0
                      ? ((copy.successfulTrades / copy.copiedTrades) * 100).toFixed(1)
                      : 0}%
                  </span>
                </div>
              </div>

              <div className="copy-info">
                <div className="info-row">
                  <span>Copy Amount:</span>
                  <span>${parseFloat(copy.copyAmount).toFixed(2)}</span>
                </div>
                <div className="info-row">
                  <span>Copy Ratio:</span>
                  <span>{parseFloat(copy.copyRatio).toFixed(0)}%</span>
                </div>
                <div className="info-row">
                  <span>Started:</span>
                  <span>{new Date(copy.startedAt).toLocaleDateString()}</span>
                </div>
                {copy.lastCopiedAt && (
                  <div className="info-row">
                    <span>Last Copied:</span>
                    <span>{new Date(copy.lastCopiedAt).toLocaleString()}</span>
                  </div>
                )}
              </div>

              {copy.status === 'ACTIVE' && (
                <div className="copy-actions">
                  <button className="btn-stop" onClick={() => stopCopy(copy.id)}>
                    Stop Copy
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyCopies;
