import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { PriceUpdate } from '../types/coin';

class WebSocketService {
    private client: Client | null = null;
    private subscribers: Map<string, (update: PriceUpdate) => void> = new Map();

    connect() {
        const socket = new SockJS('http://localhost:8080/ws');
        this.client = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                console.log('Connected to WebSocket');
                // 기존 구독 복구
                this.subscribers.forEach((callback, symbol) => {
                    this.subscribeToCoin(symbol, callback);
                });
            },
            onDisconnect: () => {
                console.log('Disconnected from WebSocket');
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
            }
        });

        this.client.activate();
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.client = null;
        }
    }

    subscribeToCoin(symbol: string, callback: (update: PriceUpdate) => void) {
        if (!this.client) {
            this.subscribers.set(symbol, callback);
            return;
        }

        this.client.subscribe(`/topic/price/${symbol}`, (message) => {
            const update: PriceUpdate = JSON.parse(message.body);
            callback(update);
        });
    }

    unsubscribeFromCoin(symbol: string) {
        this.subscribers.delete(symbol);
    }
}

export const websocketService = new WebSocketService(); 