import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './UserManagement.css';

interface User {
  id: number;
  username: string;
  role: string;
  enabled: boolean;
  tradingEnabled: boolean;
  withdrawalEnabled: boolean;
  usdtBalance: string;
  btcBalance: string;
  ethBalance: string;
}

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const response = await axios.get<{ content: User[] }>(
        '/api/admin/users?page=0&size=50',
        config
      );

      setUsers(response.data.content || []);
    } catch (error) {
      console.error('Failed to load users:', error);
    } finally {
      setLoading(false);
    }
  };

  const searchUsers = async () => {
    if (!searchKeyword.trim()) {
      loadUsers();
      return;
    }

    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const response = await axios.get<User[]>(
        `/api/admin/users/search?keyword=${searchKeyword}`,
        config
      );

      setUsers(response.data);
    } catch (error) {
      console.error('Failed to search users:', error);
    } finally {
      setLoading(false);
    }
  };

  const toggleUserEnabled = async (userId: number) => {
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(`/api/admin/users/${userId}/toggle-enabled`, {}, config);

      alert('User enabled status updated successfully');
      loadUsers();
    } catch (error) {
      console.error('Failed to toggle user enabled:', error);
      alert('Failed to update user status');
    }
  };

  const setTradingEnabled = async (userId: number, enabled: boolean) => {
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(
        `/api/admin/users/${userId}/set-trading?enabled=${enabled}`,
        {},
        config
      );

      alert('Trading status updated successfully');
      loadUsers();
    } catch (error) {
      console.error('Failed to update trading status:', error);
      alert('Failed to update trading status');
    }
  };

  const setWithdrawalEnabled = async (userId: number, enabled: boolean) => {
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(
        `/api/admin/users/${userId}/set-withdrawal?enabled=${enabled}`,
        {},
        config
      );

      alert('Withdrawal status updated successfully');
      loadUsers();
    } catch (error) {
      console.error('Failed to update withdrawal status:', error);
      alert('Failed to update withdrawal status');
    }
  };

  const viewUserDetails = (user: User) => {
    setSelectedUser(user);
  };

  const closeModal = () => {
    setSelectedUser(null);
  };

  return (
    <div className="user-management">
      <h1>User Management</h1>

      {/* Search Bar */}
      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by username..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && searchUsers()}
        />
        <button onClick={searchUsers}>Search</button>
        <button onClick={loadUsers}>Show All</button>
      </div>

      {/* Users Table */}
      {loading ? (
        <div className="loading">Loading users...</div>
      ) : (
        <table className="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Role</th>
              <th>Status</th>
              <th>Trading</th>
              <th>Withdrawal</th>
              <th>USDT Balance</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.username}</td>
                <td>
                  <span className={`role ${user.role.toLowerCase()}`}>{user.role}</span>
                </td>
                <td>
                  <span className={`status ${user.enabled ? 'active' : 'inactive'}`}>
                    {user.enabled ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td>
                  <span className={`status ${user.tradingEnabled ? 'enabled' : 'disabled'}`}>
                    {user.tradingEnabled ? 'Enabled' : 'Disabled'}
                  </span>
                </td>
                <td>
                  <span className={`status ${user.withdrawalEnabled ? 'enabled' : 'disabled'}`}>
                    {user.withdrawalEnabled ? 'Enabled' : 'Disabled'}
                  </span>
                </td>
                <td>{parseFloat(user.usdtBalance).toFixed(2)}</td>
                <td className="actions">
                  <button className="btn-small" onClick={() => viewUserDetails(user)}>
                    View
                  </button>
                  <button
                    className="btn-small"
                    onClick={() => toggleUserEnabled(user.id)}
                  >
                    Toggle Status
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* User Details Modal */}
      {selectedUser && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>User Details</h2>
            <div className="user-details">
              <div className="detail-row">
                <span className="label">ID:</span>
                <span className="value">{selectedUser.id}</span>
              </div>
              <div className="detail-row">
                <span className="label">Username:</span>
                <span className="value">{selectedUser.username}</span>
              </div>
              <div className="detail-row">
                <span className="label">Role:</span>
                <span className="value">{selectedUser.role}</span>
              </div>
              <div className="detail-row">
                <span className="label">Account Status:</span>
                <span className={`value ${selectedUser.enabled ? 'active' : 'inactive'}`}>
                  {selectedUser.enabled ? 'Active' : 'Inactive'}
                </span>
              </div>
              <div className="detail-row">
                <span className="label">USDT Balance:</span>
                <span className="value">{parseFloat(selectedUser.usdtBalance).toFixed(2)}</span>
              </div>
              <div className="detail-row">
                <span className="label">BTC Balance:</span>
                <span className="value">{parseFloat(selectedUser.btcBalance).toFixed(8)}</span>
              </div>
              <div className="detail-row">
                <span className="label">ETH Balance:</span>
                <span className="value">{parseFloat(selectedUser.ethBalance).toFixed(8)}</span>
              </div>

              <div className="actions-section">
                <h3>User Controls</h3>
                <div className="control-buttons">
                  <button
                    className={selectedUser.tradingEnabled ? 'btn-danger' : 'btn-success'}
                    onClick={() => setTradingEnabled(selectedUser.id, !selectedUser.tradingEnabled)}
                  >
                    {selectedUser.tradingEnabled ? 'Disable Trading' : 'Enable Trading'}
                  </button>
                  <button
                    className={selectedUser.withdrawalEnabled ? 'btn-danger' : 'btn-success'}
                    onClick={() => setWithdrawalEnabled(selectedUser.id, !selectedUser.withdrawalEnabled)}
                  >
                    {selectedUser.withdrawalEnabled ? 'Disable Withdrawal' : 'Enable Withdrawal'}
                  </button>
                  <button
                    className="btn-warning"
                    onClick={() => toggleUserEnabled(selectedUser.id)}
                  >
                    Toggle Account Status
                  </button>
                </div>
              </div>
            </div>
            <button className="close-btn" onClick={closeModal}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;
