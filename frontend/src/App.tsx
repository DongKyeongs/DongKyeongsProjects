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

const App: React.FC = () => {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/exchange" element={<Exchange />} />
        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
};

export default App; 