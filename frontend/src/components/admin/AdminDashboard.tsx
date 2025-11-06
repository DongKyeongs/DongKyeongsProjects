import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './AdminDashboard.css';

interface DashboardStats {
  totalUsers: number;
  newUsersToday: number;
  activeUsers: number;
  pendingKyc: number;
  openAmlAlerts: number;
  totalTrades: number;
  todayVolume: string;
  todayFees: string;
  totalOrders: number;
  activeOrders: number;
}

interface RecentTrade {
  id: number;
  symbol: string;
  price: string;
  amount: string;
  side: string;
  executedAt: string;
}

interface RecentUser {
  id: number;
  username: string;
  role: string;
  enabled: boolean;
}

const AdminDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentTrades, setRecentTrades] = useState<RecentTrade[]>([]);
  const [recentUsers, setRecentUsers] = useState<RecentUser[]>([]);
  const [volumeBySymbol, setVolumeBySymbol] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const [statsRes, tradesRes, usersRes, volumeRes] = await Promise.all([
        axios.get<DashboardStats>('/api/admin/dashboard/stats', config),
        axios.get<RecentTrade[]>('/api/admin/dashboard/recent-trades?limit=10', config),
        axios.get<RecentUser[]>('/api/admin/dashboard/recent-users?limit=10', config),
        axios.get<{ volumes: Record<string, string> }>('/api/admin/dashboard/volume-by-symbol', config),
      ]);

      setStats(statsRes.data);
      setRecentTrades(tradesRes.data);
      setRecentUsers(usersRes.data);
      setVolumeBySymbol(volumeRes.data.volumes);
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="admin-dashboard loading">Loading dashboard...</div>;
  }

  if (!stats) {
    return <div className="admin-dashboard error">Failed to load dashboard data</div>;
  }

  return (
    <div className="admin-dashboard">
      <h1>Admin Dashboard</h1>

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Users</h3>
          <div className="stat-value">{stats.totalUsers}</div>
          <div className="stat-subtext">Active: {stats.activeUsers}</div>
        </div>

        <div className="stat-card">
          <h3>New Users Today</h3>
          <div className="stat-value">{stats.newUsersToday}</div>
        </div>

        <div className="stat-card">
          <h3>Total Trades</h3>
          <div className="stat-value">{stats.totalTrades}</div>
          <div className="stat-subtext">Today Volume: {parseFloat(stats.todayVolume).toFixed(2)}</div>
        </div>

        <div className="stat-card">
          <h3>Today Fees</h3>
          <div className="stat-value">{parseFloat(stats.todayFees).toFixed(4)}</div>
          <div className="stat-subtext">Revenue</div>
        </div>

        <div className="stat-card">
          <h3>Total Orders</h3>
          <div className="stat-value">{stats.totalOrders}</div>
          <div className="stat-subtext">Active: {stats.activeOrders}</div>
        </div>

        <div className="stat-card">
          <h3>Pending KYC</h3>
          <div className="stat-value">{stats.pendingKyc}</div>
        </div>

        <div className="stat-card warning">
          <h3>AML Alerts</h3>
          <div className="stat-value">{stats.openAmlAlerts}</div>
          <div className="stat-subtext">Open alerts</div>
        </div>
      </div>

      {/* Volume by Symbol */}
      <div className="section">
        <h2>Trading Volume by Symbol</h2>
        <div className="volume-grid">
          {Object.entries(volumeBySymbol).map(([symbol, volume]) => (
            <div key={symbol} className="volume-item">
              <span className="symbol">{symbol}</span>
              <span className="volume">{parseFloat(volume).toFixed(2)}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Trades */}
      <div className="section">
        <h2>Recent Trades</h2>
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Symbol</th>
              <th>Price</th>
              <th>Amount</th>
              <th>Side</th>
              <th>Executed At</th>
            </tr>
          </thead>
          <tbody>
            {recentTrades.map((trade) => (
              <tr key={trade.id}>
                <td>{trade.id}</td>
                <td>{trade.symbol}</td>
                <td>{parseFloat(trade.price).toFixed(2)}</td>
                <td>{parseFloat(trade.amount).toFixed(4)}</td>
                <td className={`side-${trade.side.toLowerCase()}`}>{trade.side}</td>
                <td>{new Date(trade.executedAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Recent Users */}
      <div className="section">
        <h2>Recent Users</h2>
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Role</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {recentUsers.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.username}</td>
                <td>{user.role}</td>
                <td>
                  <span className={`status ${user.enabled ? 'active' : 'inactive'}`}>
                    {user.enabled ? 'Active' : 'Inactive'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminDashboard;
