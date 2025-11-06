import React from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import './AdminLayout.css';

const AdminLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const isActive = (path: string) => {
    return location.pathname === path ? 'active' : '';
  };

  return (
    <div className="admin-layout">
      <div className="admin-sidebar">
        <div className="admin-logo">
          <h2>Admin Panel</h2>
        </div>
        <nav className="admin-nav">
          <Link to="/admin" className={isActive('/admin')}>
            Dashboard
          </Link>
          <Link to="/admin/users" className={isActive('/admin/users')}>
            User Management
          </Link>
          <Link to="/admin/fees" className={isActive('/admin/fees')}>
            Fee Management
          </Link>
          <Link to="/admin/kyc-aml" className={isActive('/admin/kyc-aml')}>
            KYC & AML
          </Link>
        </nav>
        <div className="admin-footer">
          <button onClick={() => navigate('/dashboard')} className="btn-back">
            Back to Exchange
          </button>
          <button onClick={handleLogout} className="btn-logout">
            Logout
          </button>
        </div>
      </div>
      <div className="admin-content">
        <Outlet />
      </div>
    </div>
  );
};

export default AdminLayout;
