import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import CoinList from './components/CoinList';
import ErrorBoundary from './components/ErrorBoundary';
import WalletConnect from './components/WalletConnect';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Exchange from './components/Exchange';
import AdminLayout from './components/admin/AdminLayout';
import AdminDashboard from './components/admin/AdminDashboard';
import UserManagement from './components/admin/UserManagement';
import FeeManagement from './components/admin/FeeManagement';
import FeeAnalytics from './components/admin/FeeAnalytics';
import VipManagement from './components/admin/VipManagement';
import KycAmlManagement from './components/admin/KycAmlManagement';

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/exchange" element={<Exchange />} />

        {/* Admin Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="users" element={<UserManagement />} />
          <Route path="fees" element={<FeeManagement />} />
          <Route path="fee-analytics" element={<FeeAnalytics />} />
          <Route path="vip" element={<VipManagement />} />
          <Route path="kyc-aml" element={<KycAmlManagement />} />
        </Route>

        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
};

export default App; 