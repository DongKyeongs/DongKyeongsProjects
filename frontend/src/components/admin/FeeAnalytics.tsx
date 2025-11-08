import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './FeeAnalytics.css';

interface OverallStats {
  totalFees: string;
  makerFees: string;
  takerFees: string;
  withdrawalFees: string;
  totalRefunded: string;
  netRevenue: string;
  transactionCount: number;
}

interface SymbolStats {
  symbol: string;
  totalFees: string;
  transactionCount: number;
}

interface VipStats {
  vipLevel: string;
  userCount: number;
  totalFeesPaid: string;
  totalRefunded: string;
  netRevenue: string;
}

interface DailyStats {
  date: string;
  totalFees: string;
  transactionCount: number;
}

const FeeAnalytics: React.FC = () => {
  const [overallStats, setOverallStats] = useState<OverallStats | null>(null);
  const [symbolStats, setSymbolStats] = useState<SymbolStats[]>([]);
  const [vipStats, setVipStats] = useState<VipStats[]>([]);
  const [dailyStats, setDailyStats] = useState<DailyStats[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAllStats();
  }, []);

  const loadAllStats = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const [overall, bySymbol, byVip, daily] = await Promise.all([
        axios.get<OverallStats>('/api/admin/fee-analytics/overall', config),
        axios.get<SymbolStats[]>('/api/admin/fee-analytics/by-symbol', config),
        axios.get<VipStats[]>('/api/admin/fee-analytics/by-vip', config),
        axios.get<DailyStats[]>('/api/admin/fee-analytics/daily?days=30', config),
      ]);

      setOverallStats(overall.data);
      setSymbolStats(bySymbol.data);
      setVipStats(byVip.data);
      setDailyStats(daily.data);
    } catch (error) {
      console.error('Failed to load fee analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="fee-analytics loading">Loading analytics...</div>;
  }

  return (
    <div className="fee-analytics">
      <h1>Fee Analytics Dashboard</h1>

      {/* Overall Stats */}
      {overallStats && (
        <div className="overall-stats">
          <h2>Overall Fee Statistics</h2>
          <div className="stats-grid">
            <div className="stat-card primary">
              <h3>Total Fees Collected</h3>
              <div className="value">${parseFloat(overallStats.totalFees).toFixed(2)}</div>
            </div>
            <div className="stat-card">
              <h3>Maker Fees</h3>
              <div className="value">${parseFloat(overallStats.makerFees).toFixed(2)}</div>
            </div>
            <div className="stat-card">
              <h3>Taker Fees</h3>
              <div className="value">${parseFloat(overallStats.takerFees).toFixed(2)}</div>
            </div>
            <div className="stat-card">
              <h3>Withdrawal Fees</h3>
              <div className="value">${parseFloat(overallStats.withdrawalFees).toFixed(2)}</div>
            </div>
            <div className="stat-card warning">
              <h3>Total Refunded</h3>
              <div className="value">${parseFloat(overallStats.totalRefunded).toFixed(2)}</div>
            </div>
            <div className="stat-card success">
              <h3>Net Revenue</h3>
              <div className="value">${parseFloat(overallStats.netRevenue).toFixed(2)}</div>
            </div>
            <div className="stat-card">
              <h3>Total Transactions</h3>
              <div className="value">{overallStats.transactionCount.toLocaleString()}</div>
            </div>
          </div>
        </div>
      )}

      {/* By Symbol */}
      <div className="section">
        <h2>Fees by Symbol</h2>
        <table className="data-table">
          <thead>
            <tr>
              <th>Symbol</th>
              <th>Total Fees</th>
              <th>Transactions</th>
            </tr>
          </thead>
          <tbody>
            {symbolStats.slice(0, 10).map((stat) => (
              <tr key={stat.symbol}>
                <td className="symbol-name">{stat.symbol}</td>
                <td className="amount">${parseFloat(stat.totalFees).toFixed(2)}</td>
                <td>{stat.transactionCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* By VIP Level */}
      <div className="section">
        <h2>Fees by VIP Level</h2>
        <table className="data-table">
          <thead>
            <tr>
              <th>VIP Level</th>
              <th>User Count</th>
              <th>Total Paid</th>
              <th>Total Refunded</th>
              <th>Net Revenue</th>
            </tr>
          </thead>
          <tbody>
            {vipStats.map((stat) => (
              <tr key={stat.vipLevel}>
                <td>
                  <span className={`vip-badge ${stat.vipLevel.toLowerCase()}`}>
                    {stat.vipLevel}
                  </span>
                </td>
                <td>{stat.userCount}</td>
                <td className="amount">${parseFloat(stat.totalFeesPaid).toFixed(2)}</td>
                <td className="amount">${parseFloat(stat.totalRefunded).toFixed(2)}</td>
                <td className="amount success">${parseFloat(stat.netRevenue).toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Daily Trend (Last 7 days) */}
      <div className="section">
        <h2>Daily Fee Trend (Last 7 Days)</h2>
        <div className="chart-container">
          {dailyStats.slice(-7).map((stat) => {
            const maxFee = Math.max(...dailyStats.slice(-7).map(s => parseFloat(s.totalFees)));
            const height = (parseFloat(stat.totalFees) / maxFee) * 200;

            return (
              <div key={stat.date} className="bar-item">
                <div className="bar" style={{ height: `${height}px` }}>
                  <span className="bar-value">${parseFloat(stat.totalFees).toFixed(0)}</span>
                </div>
                <div className="bar-label">{new Date(stat.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default FeeAnalytics;
