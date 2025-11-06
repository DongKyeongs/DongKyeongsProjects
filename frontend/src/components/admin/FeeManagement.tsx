import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './FeeManagement.css';

interface Fee {
  id: number;
  symbol: string;
  type: 'MAKER' | 'TAKER' | 'WITHDRAWAL';
  rate: string;
  minFee: string;
  maxFee: string;
  active: boolean;
}

const FeeManagement: React.FC = () => {
  const [fees, setFees] = useState<Fee[]>([]);
  const [loading, setLoading] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingFee, setEditingFee] = useState<Fee | null>(null);

  const [formData, setFormData] = useState({
    symbol: '',
    type: 'MAKER' as 'MAKER' | 'TAKER' | 'WITHDRAWAL',
    rate: '',
    minFee: '',
    maxFee: '',
    active: true,
  });

  useEffect(() => {
    loadFees();
  }, []);

  const loadFees = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const response = await axios.get<Fee[]>('/api/admin/fees', config);
      setFees(response.data);
    } catch (error) {
      console.error('Failed to load fees:', error);
    } finally {
      setLoading(false);
    }
  };

  const openCreateModal = () => {
    setFormData({
      symbol: '',
      type: 'MAKER',
      rate: '',
      minFee: '',
      maxFee: '',
      active: true,
    });
    setEditingFee(null);
    setShowCreateModal(true);
  };

  const openEditModal = (fee: Fee) => {
    setFormData({
      symbol: fee.symbol,
      type: fee.type,
      rate: fee.rate,
      minFee: fee.minFee,
      maxFee: fee.maxFee,
      active: fee.active,
    });
    setEditingFee(fee);
    setShowCreateModal(true);
  };

  const closeModal = () => {
    setShowCreateModal(false);
    setEditingFee(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      if (editingFee) {
        // Update existing fee
        await axios.put(`/api/admin/fees/${editingFee.id}`, formData, config);
        alert('Fee updated successfully');
      } else {
        // Create new fee
        await axios.post('/api/admin/fees', formData, config);
        alert('Fee created successfully');
      }

      closeModal();
      loadFees();
    } catch (error) {
      console.error('Failed to save fee:', error);
      alert('Failed to save fee');
    }
  };

  const toggleFeeActive = async (feeId: number) => {
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(`/api/admin/fees/${feeId}/toggle-active`, {}, config);
      alert('Fee status updated successfully');
      loadFees();
    } catch (error) {
      console.error('Failed to toggle fee status:', error);
      alert('Failed to update fee status');
    }
  };

  const deleteFee = async (feeId: number) => {
    if (!window.confirm('Are you sure you want to delete this fee?')) {
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.delete(`/api/admin/fees/${feeId}`, config);
      alert('Fee deleted successfully');
      loadFees();
    } catch (error) {
      console.error('Failed to delete fee:', error);
      alert('Failed to delete fee');
    }
  };

  const getFeeTypeColor = (type: string) => {
    switch (type) {
      case 'MAKER':
        return 'type-maker';
      case 'TAKER':
        return 'type-taker';
      case 'WITHDRAWAL':
        return 'type-withdrawal';
      default:
        return '';
    }
  };

  return (
    <div className="fee-management">
      <div className="header">
        <h1>Fee Management</h1>
        <button className="btn-create" onClick={openCreateModal}>
          Create New Fee
        </button>
      </div>

      {loading ? (
        <div className="loading">Loading fees...</div>
      ) : (
        <table className="fees-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Symbol</th>
              <th>Type</th>
              <th>Rate (%)</th>
              <th>Min Fee</th>
              <th>Max Fee</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {fees.map((fee) => (
              <tr key={fee.id}>
                <td>{fee.id}</td>
                <td>{fee.symbol}</td>
                <td>
                  <span className={`fee-type ${getFeeTypeColor(fee.type)}`}>
                    {fee.type}
                  </span>
                </td>
                <td>{(parseFloat(fee.rate) * 100).toFixed(2)}%</td>
                <td>{parseFloat(fee.minFee).toFixed(8)}</td>
                <td>{parseFloat(fee.maxFee).toFixed(2)}</td>
                <td>
                  <span className={`status ${fee.active ? 'active' : 'inactive'}`}>
                    {fee.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="actions">
                  <button className="btn-small btn-edit" onClick={() => openEditModal(fee)}>
                    Edit
                  </button>
                  <button
                    className="btn-small btn-toggle"
                    onClick={() => toggleFeeActive(fee.id)}
                  >
                    Toggle
                  </button>
                  <button
                    className="btn-small btn-delete"
                    onClick={() => deleteFee(fee.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Create/Edit Modal */}
      {showCreateModal && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingFee ? 'Edit Fee' : 'Create New Fee'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Symbol</label>
                <input
                  type="text"
                  name="symbol"
                  value={formData.symbol}
                  onChange={handleInputChange}
                  placeholder="e.g., BTC/USDT"
                  required
                />
              </div>

              <div className="form-group">
                <label>Fee Type</label>
                <select name="type" value={formData.type} onChange={handleInputChange} required>
                  <option value="MAKER">MAKER</option>
                  <option value="TAKER">TAKER</option>
                  <option value="WITHDRAWAL">WITHDRAWAL</option>
                </select>
              </div>

              <div className="form-group">
                <label>Rate (decimal, e.g., 0.001 = 0.1%)</label>
                <input
                  type="number"
                  name="rate"
                  value={formData.rate}
                  onChange={handleInputChange}
                  step="0.0001"
                  min="0"
                  max="1"
                  required
                />
              </div>

              <div className="form-group">
                <label>Minimum Fee</label>
                <input
                  type="number"
                  name="minFee"
                  value={formData.minFee}
                  onChange={handleInputChange}
                  step="0.00000001"
                  min="0"
                  required
                />
              </div>

              <div className="form-group">
                <label>Maximum Fee</label>
                <input
                  type="number"
                  name="maxFee"
                  value={formData.maxFee}
                  onChange={handleInputChange}
                  step="0.01"
                  min="0"
                  required
                />
              </div>

              <div className="form-group checkbox-group">
                <label>
                  <input
                    type="checkbox"
                    name="active"
                    checked={formData.active}
                    onChange={handleInputChange}
                  />
                  <span>Active</span>
                </label>
              </div>

              <div className="form-actions">
                <button type="submit" className="btn-submit">
                  {editingFee ? 'Update Fee' : 'Create Fee'}
                </button>
                <button type="button" className="btn-cancel" onClick={closeModal}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default FeeManagement;
