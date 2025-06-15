import React, { useState, useEffect } from 'react';
import { connectWallet, disconnectWallet, switchNetwork, getWalletEvents, WalletInfo, formatAddress } from '../utils/web3';
import './WalletConnect.css';

const WalletConnect: React.FC = () => {
    const [walletInfo, setWalletInfo] = useState<WalletInfo | null>(null);
    const [isConnecting, setIsConnecting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleConnect = async () => {
        setIsConnecting(true);
        setError(null);
        
        try {
            const info = await connectWallet();
            setWalletInfo(info);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setIsConnecting(false);
        }
    };

    const handleDisconnect = async () => {
        try {
            await disconnectWallet();
            setWalletInfo(null);
        } catch (err: any) {
            setError(err.message);
        }
    };

    const handleSwitchNetwork = async (chainId: number) => {
        try {
            await switchNetwork(chainId);
        } catch (err: any) {
            setError(err.message);
        }
    };

    useEffect(() => {
        if (!window.ethereum) return;

        const handleAccountsChanged = (accounts: string[]) => {
            if (accounts.length === 0) {
                setWalletInfo(null);
            } else {
                handleConnect();
            }
        };

        const handleChainChanged = () => {
            handleConnect();
        };

        const handleDisconnect = () => {
            setWalletInfo(null);
        };

        window.ethereum.on('accountsChanged', handleAccountsChanged);
        window.ethereum.on('chainChanged', handleChainChanged);
        window.ethereum.on('disconnect', handleDisconnect);

        return () => {
            if (window.ethereum) {
                window.ethereum.removeListener('accountsChanged', handleAccountsChanged);
                window.ethereum.removeListener('chainChanged', handleChainChanged);
                window.ethereum.removeListener('disconnect', handleDisconnect);
            }
        };
    }, []);

    return (
        <div className="wallet-connect">
            {!walletInfo ? (
                <button 
                    className="connect-button"
                    onClick={handleConnect}
                    disabled={isConnecting}
                >
                    {isConnecting ? '연결 중...' : '지갑 연결'}
                </button>
            ) : (
                <div className="wallet-info">
                    <div className="wallet-address">
                        <span>{formatAddress(walletInfo.address)}</span>
                        <span className="network-badge">{walletInfo.network}</span>
                    </div>
                    <div className="wallet-balance">
                        {parseFloat(walletInfo.balance).toFixed(4)} ETH
                    </div>
                    <div className="wallet-actions">
                        <button 
                            className="switch-network"
                            onClick={() => handleSwitchNetwork(1)} // Ethereum Mainnet
                        >
                            메인넷
                        </button>
                        <button 
                            className="switch-network"
                            onClick={() => handleSwitchNetwork(56)} // BSC
                        >
                            BSC
                        </button>
                        <button 
                            className="disconnect-button"
                            onClick={handleDisconnect}
                        >
                            연결 해제
                        </button>
                    </div>
                </div>
            )}
            
            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}
        </div>
    );
};

export default WalletConnect; 