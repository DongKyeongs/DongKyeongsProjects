import { ethers } from 'ethers';

declare global {
    interface Window {
        ethereum: any;
    }
}

export interface WalletInfo {
    address: string;
    network: string;
    balance: string;
}

export const connectWallet = async (): Promise<WalletInfo> => {
    if (!window.ethereum) throw new Error('메타마스크가 설치되어 있지 않습니다.');
    
    const provider = new ethers.BrowserProvider(window.ethereum);
    const accounts = await provider.send('eth_requestAccounts', []);
    const network = await provider.getNetwork();
    const balance = await provider.getBalance(accounts[0]);
    
    return {
        address: accounts[0],
        network: network.name,
        balance: ethers.formatEther(balance)
    };
};

export const disconnectWallet = async (): Promise<void> => {
    // 메타마스크는 실제로 연결 해제 기능을 제공하지 않습니다.
    // 대신 상태만 초기화합니다.
};

export const switchNetwork = async (chainId: number): Promise<void> => {
    if (!window.ethereum) throw new Error('메타마스크가 설치되어 있지 않습니다.');
    await window.ethereum.request({
        method: 'wallet_switchEthereumChain',
        params: [{ chainId: `0x${chainId.toString(16)}` }],
    });
};

export const getWalletEvents = () => {
    if (!window.ethereum) return null;
    return window.ethereum;
};

export const formatAddress = (address: string): string => {
    return `${address.slice(0, 6)}...${address.slice(-4)}`;
}; 