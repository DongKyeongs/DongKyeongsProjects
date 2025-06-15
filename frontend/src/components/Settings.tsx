import React, { useState, useEffect } from 'react';
import { settingsApi } from '../api/settings';

interface ExchangeSettings {
    theme: string;
    language: string;
    priceAlertsEnabled: boolean;
    orderNotificationsEnabled: boolean;
    systemNotificationsEnabled: boolean;
    makerFee: number;
    takerFee: number;
    minTradeAmount: number;
    maxTradeAmount: number;
}

const Settings: React.FC = () => {
    const [settings, setSettings] = useState<ExchangeSettings | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        const fetchSettings = async () => {
            try {
                const data = await settingsApi.getSettings('user1'); // 실제로는 로그인된 사용자 ID 사용
                setSettings(data);
                setIsLoading(false);
            } catch (err) {
                setError('설정 로딩 실패');
                setIsLoading(false);
            }
        };

        fetchSettings();
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!settings) return;

        setIsSaving(true);
        try {
            const updatedSettings = await settingsApi.updateSettings('user1', settings);
            setSettings(updatedSettings);
            alert('설정이 저장되었습니다');
        } catch (err) {
            setError('설정 저장 실패');
        } finally {
            setIsSaving(false);
        }
    };

    if (isLoading) return <div className="text-center py-4">로딩중...</div>;
    if (error) return <div className="text-center text-red-500 py-4">{error}</div>;
    if (!settings) return null;

    return (
        <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-2xl font-bold mb-6">거래소 설정</h2>
            <form onSubmit={handleSubmit} className="space-y-6">
                {/* 테마 설정 */}
                <div>
                    <label className="block text-sm font-medium text-gray-700">테마</label>
                    <select
                        value={settings.theme}
                        onChange={(e) => setSettings({ ...settings, theme: e.target.value })}
                        className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
                    >
                        <option value="light">라이트</option>
                        <option value="dark">다크</option>
                    </select>
                </div>

                {/* 언어 설정 */}
                <div>
                    <label className="block text-sm font-medium text-gray-700">언어</label>
                    <select
                        value={settings.language}
                        onChange={(e) => setSettings({ ...settings, language: e.target.value })}
                        className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
                    >
                        <option value="ko">한국어</option>
                        <option value="en">English</option>
                    </select>
                </div>

                {/* 알림 설정 */}
                <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-4">알림 설정</h3>
                    <div className="space-y-4">
                        <label className="flex items-center">
                            <input
                                type="checkbox"
                                checked={settings.priceAlertsEnabled}
                                onChange={(e) => setSettings({ ...settings, priceAlertsEnabled: e.target.checked })}
                                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            />
                            <span className="ml-2 text-sm text-gray-700">가격 알림</span>
                        </label>
                        <label className="flex items-center">
                            <input
                                type="checkbox"
                                checked={settings.orderNotificationsEnabled}
                                onChange={(e) => setSettings({ ...settings, orderNotificationsEnabled: e.target.checked })}
                                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            />
                            <span className="ml-2 text-sm text-gray-700">주문 알림</span>
                        </label>
                        <label className="flex items-center">
                            <input
                                type="checkbox"
                                checked={settings.systemNotificationsEnabled}
                                onChange={(e) => setSettings({ ...settings, systemNotificationsEnabled: e.target.checked })}
                                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            />
                            <span className="ml-2 text-sm text-gray-700">시스템 알림</span>
                        </label>
                    </div>
                </div>

                {/* 수수료 설정 */}
                <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-4">수수료 설정</h3>
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">메이커 수수료 (%)</label>
                            <input
                                type="number"
                                value={settings.makerFee * 100}
                                onChange={(e) => setSettings({ ...settings, makerFee: parseFloat(e.target.value) / 100 })}
                                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                step="0.01"
                                min="0"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">테이커 수수료 (%)</label>
                            <input
                                type="number"
                                value={settings.takerFee * 100}
                                onChange={(e) => setSettings({ ...settings, takerFee: parseFloat(e.target.value) / 100 })}
                                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                step="0.01"
                                min="0"
                            />
                        </div>
                    </div>
                </div>

                {/* 거래 제한 설정 */}
                <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-4">거래 제한 설정</h3>
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">최소 거래 금액 (USDT)</label>
                            <input
                                type="number"
                                value={settings.minTradeAmount}
                                onChange={(e) => setSettings({ ...settings, minTradeAmount: parseFloat(e.target.value) })}
                                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                step="0.01"
                                min="0"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">최대 거래 금액 (USDT)</label>
                            <input
                                type="number"
                                value={settings.maxTradeAmount}
                                onChange={(e) => setSettings({ ...settings, maxTradeAmount: parseFloat(e.target.value) })}
                                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                                step="0.01"
                                min="0"
                            />
                        </div>
                    </div>
                </div>

                <div className="flex justify-end">
                    <button
                        type="submit"
                        disabled={isSaving}
                        className="px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                    >
                        {isSaving ? '저장 중...' : '설정 저장'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default Settings; 