import React, { useState } from 'react';
import { notificationApi } from '../api/notification';

interface PriceAlertFormProps {
    pair: string;
    currentPrice: number;
}

const PriceAlertForm: React.FC<PriceAlertFormProps> = ({ pair, currentPrice }) => {
    const [targetPrice, setTargetPrice] = useState('');
    const [isAbove, setIsAbove] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        try {
            const price = parseFloat(targetPrice);
            if (isNaN(price) || price <= 0) {
                throw new Error('유효한 가격을 입력해주세요');
            }

            await notificationApi.createPriceAlert('user1', pair, price, isAbove);
            setTargetPrice('');
            alert('가격 알림이 설정되었습니다');
        } catch (err) {
            setError(err instanceof Error ? err.message : '알림 설정 실패');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="bg-white shadow rounded-lg p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">가격 알림 설정</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700">현재 가격</label>
                    <div className="mt-1 text-lg font-semibold text-gray-900">
                        ${currentPrice.toLocaleString()}
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700">목표 가격</label>
                    <input
                        type="number"
                        value={targetPrice}
                        onChange={(e) => setTargetPrice(e.target.value)}
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                        placeholder="목표 가격 입력"
                        step="0.01"
                        min="0"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700">알림 조건</label>
                    <div className="mt-2 space-x-4">
                        <label className="inline-flex items-center">
                            <input
                                type="radio"
                                checked={isAbove}
                                onChange={() => setIsAbove(true)}
                                className="form-radio text-blue-600"
                            />
                            <span className="ml-2">이상</span>
                        </label>
                        <label className="inline-flex items-center">
                            <input
                                type="radio"
                                checked={!isAbove}
                                onChange={() => setIsAbove(false)}
                                className="form-radio text-blue-600"
                            />
                            <span className="ml-2">이하</span>
                        </label>
                    </div>
                </div>

                {error && (
                    <div className="text-sm text-red-600">
                        {error}
                    </div>
                )}

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                >
                    {isSubmitting ? '설정 중...' : '알림 설정'}
                </button>
            </form>
        </div>
    );
};

export default PriceAlertForm; 