import React, { useState, useEffect } from 'react';
import { notificationApi } from '../api/notification';
import { connectWebSocket, disconnectWebSocket } from '../utils/websocket';

interface Notification {
    id: number;
    type: 'PRICE_ALERT' | 'ORDER_FILLED' | 'ORDER_CANCELLED' | 'SYSTEM_ALERT';
    title: string;
    message: string;
    read: boolean;
    createdAt: string;
}

const NotificationCenter: React.FC = () => {
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [isOpen, setIsOpen] = useState(false);
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        const fetchNotifications = async () => {
            try {
                const data = await notificationApi.getUserNotifications('user1'); // 실제로는 로그인된 사용자 ID 사용
                setNotifications(data);
                setUnreadCount(data.filter(n => !n.read).length);
            } catch (error) {
                console.error('알림 로딩 실패:', error);
            }
        };

        fetchNotifications();

        // WebSocket 연결
        connectWebSocket((data) => {
            if (data.type === 'NOTIFICATION') {
                setNotifications(prev => [data, ...prev]);
                setUnreadCount(prev => prev + 1);
            }
        });

        return () => {
            disconnectWebSocket();
        };
    }, []);

    const handleMarkAsRead = async (notificationId: number) => {
        try {
            await notificationApi.markAsRead('user1', notificationId);
            setNotifications(prev =>
                prev.map(n =>
                    n.id === notificationId ? { ...n, read: true } : n
                )
            );
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch (error) {
            console.error('알림 읽음 표시 실패:', error);
        }
    };

    return (
        <div className="relative">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="relative p-2 text-gray-600 hover:text-gray-900 focus:outline-none"
            >
                <svg
                    className="h-6 w-6"
                    fill="none"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                >
                    <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                {unreadCount > 0 && (
                    <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-red-600 rounded-full">
                        {unreadCount}
                    </span>
                )}
            </button>

            {isOpen && (
                <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg overflow-hidden z-50">
                    <div className="p-4 border-b">
                        <h3 className="text-lg font-medium text-gray-900">알림</h3>
                    </div>
                    <div className="max-h-96 overflow-y-auto">
                        {notifications.length === 0 ? (
                            <div className="p-4 text-center text-gray-500">
                                알림이 없습니다
                            </div>
                        ) : (
                            notifications.map((notification) => (
                                <div
                                    key={notification.id}
                                    className={`p-4 border-b hover:bg-gray-50 ${
                                        !notification.read ? 'bg-blue-50' : ''
                                    }`}
                                    onClick={() => handleMarkAsRead(notification.id)}
                                >
                                    <div className="flex justify-between items-start">
                                        <div>
                                            <p className="text-sm font-medium text-gray-900">
                                                {notification.title}
                                            </p>
                                            <p className="mt-1 text-sm text-gray-500">
                                                {notification.message}
                                            </p>
                                        </div>
                                        <span className="text-xs text-gray-400">
                                            {new Date(notification.createdAt).toLocaleString()}
                                        </span>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default NotificationCenter; 