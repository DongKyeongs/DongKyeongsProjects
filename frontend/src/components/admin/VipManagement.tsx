import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './VipManagement.css';

interface User {
  id: number;
  username: string;
  vipLevel: string;
  totalTradingVolume30d: string;
  totalFeesPaid: string;
  enabled: boolean;
}

interface VipInfo {
  currentLevel: string;
  nextLevel: string | null;
  current30dVolume: string;
  volumeToNextLevel: string;
  discountPercent: string;
  lastUpdate: string | null;
}

interface VipStats {
  vipLevel: string;
  userCount: number;
  totalFeesPaid: string;
  totalRefunded: string;
  netRevenue: string;
}

const VipManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [vipStats, setVipStats] = useState<VipStats[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [vipInfo, setVipInfo] = useState<VipInfo | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const [usersRes, statsRes] = await Promise.all([
        axios.get<{ content: User[] }>('/api/admin/users?page=0&size=100', config),
        axios.get<VipStats[]>('/api/admin/vip/stats', config),
      ]);

      setUsers(usersRes.data.content || []);
      setVipStats(statsRes.data);
    } catch (error) {
      console.error('Failed to load VIP data:', error);
    } finally {
      setLoading(false);
    }
  };

  const viewUserVipInfo = async (user: User) => {
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const response = await axios.get<VipInfo>(`/api/admin/vip/user/${user.id}`, config);
      setVipInfo(response.data);
      setSelectedUser(user);
    } catch (error) {
      console.error('Failed to load VIP info:', error);
      alert('Failed to load VIP info');
    }
  };

  const updateUserVipLevel = async (userId: number) => {
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(`/api/admin/vip/user/${userId}/update`, {}, config);
      alert('VIP level updated successfully');
      loadData();
      setSelectedUser(null);
      setVipInfo(null);
    } catch (error) {
      console.error('Failed to update VIP level:', error);
      alert('Failed to update VIP level');
    }
  };

  const setUserVipLevel = async (userId: number, level: string) => {
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(`/api/admin/vip/user/${userId}/set-level?level=${level}`, {}, config);
      alert(`VIP level set to ${level} successfully`);
      loadData();
      setSelectedUser(null);
      setVipInfo(null);
    } catch (error) {
      console.error('Failed to set VIP level:', error);
      alert('Failed to set VIP level');
    }
  };

  const updateAllVipLevels = async () => {
    if (!window.confirm('Update all users VIP levels? This may take a while.')) {
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post('/api/admin/vip/update-all', {}, config);
      alert('All VIP levels updated successfully');
      loadData();
    } catch (error) {
      console.error('Failed to update all VIP levels:', error);
      alert('Failed to update all VIP levels');
    }
  };

  const getVipBadgeClass = (level: string) => {
    return `vip-badge ${level.toLowerCase()}`;
  };

  return (
    <div className="vip-management">
      <div className="header">
        <h1>VIP Management</h1>
        <button className="btn-update-all" onClick={updateAllVipLevels}>
          Update All VIP Levels
        </button>
      </div>

      {/* VIP Stats */}
      <div className="vip-stats-section">
        <h2>VIP Statistics</h2>
        <div className="vip-stats-grid">
          {vipStats.map((stat) => (
            <div key={stat.vipLevel} className="vip-stat-card">
              <div className={getVipBadgeClass(stat.vipLevel)}>
                {stat.vipLevel}
              </div>
              <div className="stat-details">
                <div className="stat-row">
                  <span>Users:</span>
                  <span>{stat.userCount}</span>
                </div>
                <div className="stat-row">
                  <span>Total Fees:</span>
                  <span>${parseFloat(stat.totalFeesPaid).toFixed(2)}</span>
                </div>
                <div className="stat-row">
                  <span>Net Revenue:</span>
                  <span className="revenue">${parseFloat(stat.netRevenue).toFixed(2)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Users Table */}
      <div className="users-section">
        <h2>Users by VIP Level</h2>
        {loading ? (
          <div className="loading">Loading users...</div>
        ) : (
          <table className="users-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>VIP Level</th>
                <th>30d Volume</th>
                <th>Total Fees Paid</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users
                .sort((a, b) => b.totalTradingVolume30d.localeCompare(a.totalTradingVolume30d))
                .slice(0, 50)
                .map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>
                      <span className={getVipBadgeClass(user.vipLevel)}>
                        {user.vipLevel}
                      </span>
                    </td>
                    <td className="amount">${parseFloat(user.totalTradingVolume30d).toFixed(2)}</td>
                    <td className="amount">${parseFloat(user.totalFeesPaid).toFixed(2)}</td>
                    <td>
                      <button
                        className="btn-small"
                        onClick={() => viewUserVipInfo(user)}
                      >
                        View Details
                      </button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        )}
      </div>

      {/* VIP Info Modal */}
      {selectedUser && vipInfo && (
        <div className="modal-overlay" onClick={() => { setSelectedUser(null); setVipInfo(null); }}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>VIP Information - {selectedUser.username}</h2>
            <div className="vip-info-details">
              <div className="info-row">
                <span className="label">Current VIP Level:</span>
                <span className={getVipBadgeClass(vipInfo.currentLevel)}>
                  {vipInfo.currentLevel}
                </span>
              </div>
              <div className="info-row">
                <span className="label">Discount:</span>
                <span className="value">{vipInfo.discountPercent}%</span>
              </div>
              <div className="info-row">
                <span className="label">30-Day Volume:</span>
                <span className="value">${parseFloat(vipInfo.current30dVolume).toFixed(2)}</span>
              </div>
              {vipInfo.nextLevel && (
                <>
                  <div className="info-row">
                    <span className="label">Next Level:</span>
                    <span className={getVipBadgeClass(vipInfo.nextLevel)}>
                      {vipInfo.nextLevel}
                    </span>
                  </div>
                  <div className="info-row">
                    <span className="label">Volume Needed:</span>
                    <span className="value needed">
                      ${parseFloat(vipInfo.volumeToNextLevel).toFixed(2)}
                    </span>
                  </div>
                </>
              )}
              {vipInfo.lastUpdate && (
                <div className="info-row">
                  <span className="label">Last Update:</span>
                  <span className="value">
                    {new Date(vipInfo.lastUpdate).toLocaleString()}
                  </span>
                </div>
              )}
            </div>

            <div className="action-buttons">
              <h3>Admin Actions</h3>
              <button
                className="btn-action"
                onClick={() => updateUserVipLevel(selectedUser.id)}
              >
                Recalculate VIP Level
              </button>
              <div className="level-buttons">
                {['NONE', 'VIP1', 'VIP2', 'VIP3', 'VIP4', 'VIP5'].map((level) => (
                  <button
                    key={level}
                    className={`btn-level ${level.toLowerCase()}`}
                    onClick={() => setUserVipLevel(selectedUser.id, level)}
                  >
                    Set {level}
                  </button>
                ))}
              </div>
            </div>

            <button
              className="btn-close"
              onClick={() => { setSelectedUser(null); setVipInfo(null); }}
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default VipManagement;
