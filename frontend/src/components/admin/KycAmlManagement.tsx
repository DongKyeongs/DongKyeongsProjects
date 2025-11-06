import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './KycAmlManagement.css';

interface KycVerification {
  id: number;
  user: { id: number; username: string };
  fullName: string;
  dateOfBirth: string;
  nationality: string;
  address: string;
  documentType: string;
  documentNumber: string;
  status: string;
  level: string;
  submittedAt: string;
  verifiedAt?: string;
  rejectionReason?: string;
}

interface AmlAlert {
  id: number;
  user: { id: number; username: string };
  alertType: string;
  riskLevel: string;
  status: string;
  description: string;
  amount?: string;
  createdAt: string;
  resolvedAt?: string;
  investigationNotes?: string;
}

const KycAmlManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'kyc' | 'aml'>('kyc');
  const [kycList, setKycList] = useState<KycVerification[]>([]);
  const [amlAlerts, setAmlAlerts] = useState<AmlAlert[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedKyc, setSelectedKyc] = useState<KycVerification | null>(null);
  const [selectedAml, setSelectedAml] = useState<AmlAlert | null>(null);

  useEffect(() => {
    if (activeTab === 'kyc') {
      loadKycList();
    } else {
      loadAmlAlerts();
    }
  }, [activeTab]);

  const loadKycList = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const response = await axios.get<KycVerification[]>('/api/admin/kyc/all', config);
      setKycList(response.data);
    } catch (error) {
      console.error('Failed to load KYC list:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadAmlAlerts = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const response = await axios.get<AmlAlert[]>('/api/admin/aml/all', config);
      setAmlAlerts(response.data);
    } catch (error) {
      console.error('Failed to load AML alerts:', error);
    } finally {
      setLoading(false);
    }
  };

  const approveKyc = async (kycId: number, level: string) => {
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(`/api/admin/kyc/${kycId}/approve?level=${level}`, {}, config);
      alert('KYC approved successfully');
      loadKycList();
      setSelectedKyc(null);
    } catch (error) {
      console.error('Failed to approve KYC:', error);
      alert('Failed to approve KYC');
    }
  };

  const rejectKyc = async (kycId: number) => {
    const reason = prompt('Enter rejection reason:');
    if (!reason) return;

    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(`/api/admin/kyc/${kycId}/reject?reason=${encodeURIComponent(reason)}`, {}, config);
      alert('KYC rejected successfully');
      loadKycList();
      setSelectedKyc(null);
    } catch (error) {
      console.error('Failed to reject KYC:', error);
      alert('Failed to reject KYC');
    }
  };

  const updateAmlStatus = async (alertId: number, status: string) => {
    const notes = prompt('Enter investigation notes (optional):');

    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };

      await axios.post(
        `/api/admin/aml/${alertId}/update-status?status=${status}${notes ? '&notes=' + encodeURIComponent(notes) : ''}`,
        {},
        config
      );
      alert('AML alert status updated successfully');
      loadAmlAlerts();
      setSelectedAml(null);
    } catch (error) {
      console.error('Failed to update AML alert:', error);
      alert('Failed to update AML alert');
    }
  };

  const getKycStatusColor = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return 'status-approved';
      case 'REJECTED':
        return 'status-rejected';
      case 'PENDING':
      case 'REVIEWING':
        return 'status-pending';
      default:
        return '';
    }
  };

  const getRiskLevelColor = (level: string) => {
    switch (level) {
      case 'CRITICAL':
        return 'risk-critical';
      case 'HIGH':
        return 'risk-high';
      case 'MEDIUM':
        return 'risk-medium';
      case 'LOW':
        return 'risk-low';
      default:
        return '';
    }
  };

  return (
    <div className="kyc-aml-management">
      <h1>KYC & AML Management</h1>

      <div className="tabs">
        <button
          className={`tab ${activeTab === 'kyc' ? 'active' : ''}`}
          onClick={() => setActiveTab('kyc')}
        >
          KYC Verifications
        </button>
        <button
          className={`tab ${activeTab === 'aml' ? 'active' : ''}`}
          onClick={() => setActiveTab('aml')}
        >
          AML Alerts
        </button>
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : activeTab === 'kyc' ? (
        /* KYC Tab */
        <div className="kyc-section">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>User</th>
                <th>Full Name</th>
                <th>Nationality</th>
                <th>Document Type</th>
                <th>Level</th>
                <th>Status</th>
                <th>Submitted</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {kycList.map((kyc) => (
                <tr key={kyc.id}>
                  <td>{kyc.id}</td>
                  <td>{kyc.user.username}</td>
                  <td>{kyc.fullName}</td>
                  <td>{kyc.nationality}</td>
                  <td>{kyc.documentType}</td>
                  <td>
                    <span className="level-badge">{kyc.level}</span>
                  </td>
                  <td>
                    <span className={`status ${getKycStatusColor(kyc.status)}`}>
                      {kyc.status}
                    </span>
                  </td>
                  <td>{new Date(kyc.submittedAt).toLocaleDateString()}</td>
                  <td className="actions">
                    <button
                      className="btn-small btn-view"
                      onClick={() => setSelectedKyc(kyc)}
                    >
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        /* AML Tab */
        <div className="aml-section">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>User</th>
                <th>Alert Type</th>
                <th>Risk Level</th>
                <th>Status</th>
                <th>Description</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {amlAlerts.map((alert) => (
                <tr key={alert.id}>
                  <td>{alert.id}</td>
                  <td>{alert.user.username}</td>
                  <td>{alert.alertType}</td>
                  <td>
                    <span className={`risk-level ${getRiskLevelColor(alert.riskLevel)}`}>
                      {alert.riskLevel}
                    </span>
                  </td>
                  <td>{alert.status}</td>
                  <td className="description">{alert.description}</td>
                  <td>{new Date(alert.createdAt).toLocaleDateString()}</td>
                  <td className="actions">
                    <button
                      className="btn-small btn-view"
                      onClick={() => setSelectedAml(alert)}
                    >
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* KYC Details Modal */}
      {selectedKyc && (
        <div className="modal-overlay" onClick={() => setSelectedKyc(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>KYC Verification Details</h2>
            <div className="details-grid">
              <div className="detail-item">
                <span className="label">User:</span>
                <span className="value">{selectedKyc.user.username}</span>
              </div>
              <div className="detail-item">
                <span className="label">Full Name:</span>
                <span className="value">{selectedKyc.fullName}</span>
              </div>
              <div className="detail-item">
                <span className="label">Date of Birth:</span>
                <span className="value">{selectedKyc.dateOfBirth}</span>
              </div>
              <div className="detail-item">
                <span className="label">Nationality:</span>
                <span className="value">{selectedKyc.nationality}</span>
              </div>
              <div className="detail-item">
                <span className="label">Address:</span>
                <span className="value">{selectedKyc.address}</span>
              </div>
              <div className="detail-item">
                <span className="label">Document Type:</span>
                <span className="value">{selectedKyc.documentType}</span>
              </div>
              <div className="detail-item">
                <span className="label">Document Number:</span>
                <span className="value">{selectedKyc.documentNumber}</span>
              </div>
              <div className="detail-item">
                <span className="label">Status:</span>
                <span className={`value ${getKycStatusColor(selectedKyc.status)}`}>
                  {selectedKyc.status}
                </span>
              </div>
              <div className="detail-item">
                <span className="label">Level:</span>
                <span className="value">{selectedKyc.level}</span>
              </div>
              {selectedKyc.rejectionReason && (
                <div className="detail-item full-width">
                  <span className="label">Rejection Reason:</span>
                  <span className="value">{selectedKyc.rejectionReason}</span>
                </div>
              )}
            </div>

            {selectedKyc.status === 'PENDING' || selectedKyc.status === 'REVIEWING' ? (
              <div className="action-buttons">
                <button
                  className="btn-approve"
                  onClick={() => approveKyc(selectedKyc.id, 'TIER1')}
                >
                  Approve TIER1
                </button>
                <button
                  className="btn-approve"
                  onClick={() => approveKyc(selectedKyc.id, 'TIER2')}
                >
                  Approve TIER2
                </button>
                <button
                  className="btn-approve"
                  onClick={() => approveKyc(selectedKyc.id, 'TIER3')}
                >
                  Approve TIER3
                </button>
                <button className="btn-reject" onClick={() => rejectKyc(selectedKyc.id)}>
                  Reject
                </button>
              </div>
            ) : null}

            <button className="btn-close" onClick={() => setSelectedKyc(null)}>
              Close
            </button>
          </div>
        </div>
      )}

      {/* AML Alert Details Modal */}
      {selectedAml && (
        <div className="modal-overlay" onClick={() => setSelectedAml(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>AML Alert Details</h2>
            <div className="details-grid">
              <div className="detail-item">
                <span className="label">Alert ID:</span>
                <span className="value">{selectedAml.id}</span>
              </div>
              <div className="detail-item">
                <span className="label">User:</span>
                <span className="value">{selectedAml.user.username}</span>
              </div>
              <div className="detail-item">
                <span className="label">Alert Type:</span>
                <span className="value">{selectedAml.alertType}</span>
              </div>
              <div className="detail-item">
                <span className="label">Risk Level:</span>
                <span className={`value ${getRiskLevelColor(selectedAml.riskLevel)}`}>
                  {selectedAml.riskLevel}
                </span>
              </div>
              <div className="detail-item">
                <span className="label">Status:</span>
                <span className="value">{selectedAml.status}</span>
              </div>
              {selectedAml.amount && (
                <div className="detail-item">
                  <span className="label">Amount:</span>
                  <span className="value">{selectedAml.amount}</span>
                </div>
              )}
              <div className="detail-item full-width">
                <span className="label">Description:</span>
                <span className="value">{selectedAml.description}</span>
              </div>
              {selectedAml.investigationNotes && (
                <div className="detail-item full-width">
                  <span className="label">Investigation Notes:</span>
                  <span className="value">{selectedAml.investigationNotes}</span>
                </div>
              )}
              <div className="detail-item">
                <span className="label">Created:</span>
                <span className="value">{new Date(selectedAml.createdAt).toLocaleString()}</span>
              </div>
              {selectedAml.resolvedAt && (
                <div className="detail-item">
                  <span className="label">Resolved:</span>
                  <span className="value">{new Date(selectedAml.resolvedAt).toLocaleString()}</span>
                </div>
              )}
            </div>

            {selectedAml.status === 'OPEN' || selectedAml.status === 'INVESTIGATING' ? (
              <div className="action-buttons">
                <button
                  className="btn-action"
                  onClick={() => updateAmlStatus(selectedAml.id, 'INVESTIGATING')}
                >
                  Mark as Investigating
                </button>
                <button
                  className="btn-action"
                  onClick={() => updateAmlStatus(selectedAml.id, 'RESOLVED')}
                >
                  Resolve
                </button>
                <button
                  className="btn-action"
                  onClick={() => updateAmlStatus(selectedAml.id, 'FALSE_POSITIVE')}
                >
                  False Positive
                </button>
                <button
                  className="btn-action btn-escalate"
                  onClick={() => updateAmlStatus(selectedAml.id, 'ESCALATED')}
                >
                  Escalate
                </button>
              </div>
            ) : null}

            <button className="btn-close" onClick={() => setSelectedAml(null)}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default KycAmlManagement;
