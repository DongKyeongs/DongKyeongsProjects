import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let stompClient: Client | null = null;

export const connectWebSocket = (onMessage: (data: any) => void) => {
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = new Client({
        webSocketFactory: () => socket,
        onConnect: () => {
            console.log('WebSocket 연결됨');
            stompClient?.subscribe('/topic/orderbook/BTC/USDT', (message) => {
                onMessage(JSON.parse(message.body));
            });
            stompClient?.subscribe('/topic/trades/BTC/USDT', (message) => {
                onMessage(JSON.parse(message.body));
            });
        },
        onDisconnect: () => {
            console.log('WebSocket 연결 끊김');
        },
        onStompError: (error) => {
            console.error('WebSocket 에러:', error);
        }
    });

    stompClient.activate();
};

export const disconnectWebSocket = () => {
    if (stompClient) {
        stompClient.deactivate();
        stompClient = null;
    }
}; 