
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {NewMessageDto, UpdateMessageDto} from "@/lib/models/requests";

const BASE_URL = process.env.NEXT_PUBLIC_BACKEND_API_BASE_URL;

class WebSocketService {
    client: Client | null;
    connected;
    subscriptions;
    baseUrl;
    constructor() {
        this.client = null;
        this.connected = false;
        this.subscriptions = new Map();
        this.baseUrl = BASE_URL;
    }

    connect() {
        if (this.client && this.connected) {
            console.log('Already connected');
            return Promise.resolve();
        }

        return new Promise((resolve, reject) => {
            this.client = new Client({
                webSocketFactory: () => new SockJS(`${this.baseUrl}/ws`),
                debug: (str) => {
                    console.log('STOMP Debug:', str);
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });

            this.client.onConnect = (frame) => {
                console.log('Connected to WebSocket:', frame);
                this.connected = true;
                resolve(frame);
            };

            this.client.onStompError = (frame) => {
                console.error('STOMP Error:', frame.headers['message']);
                console.error('Details:', frame.body);
                this.connected = false;
                reject(new Error(frame.headers['message']));
            };

            this.client.onWebSocketClose = () => {
                console.log('WebSocket connection closed');
                this.connected = false;
            };

            this.client.activate();
        });
    }

    // Room-specific subscription methods
    subscribeToRoom(chatRoomId: number, callbacks:any) {
        if (!this.connected) {
            throw new Error('Not connected to WebSocket');
        }

        const subscriptionKeys = [];

        // Subscribe to new messages
        if (callbacks.onMessage) {
            const messagesSub = this.subscribe(`/topic/chat.${chatRoomId}`, callbacks.onMessage);
            subscriptionKeys.push(`/topic/chat.${chatRoomId}`);
        }

        // Subscribe to message deletions
        if (callbacks.onDelete) {
            const deleteSub = this.subscribe(`/topic/chat.${chatRoomId}.delete`, callbacks.onDelete);
            subscriptionKeys.push(`/topic/chat.${chatRoomId}.delete`);
        }

        // Subscribe to message edits
        if (callbacks.onEdit) {
            const editSub = this.subscribe(`/topic/chat.${chatRoomId}.edit`, callbacks.onEdit);
            subscriptionKeys.push(`/topic/chat.${chatRoomId}.edit`);
        }

        return subscriptionKeys;
    }

    unsubscribeFromRoom(chatRoomId:number) {
        this.unsubscribe(`/topic/chat.${chatRoomId}`);
        this.unsubscribe(`/topic/chat.${chatRoomId}.delete`);
        this.unsubscribe(`/topic/chat.${chatRoomId}.edit`);
    }

    subscribe(destination: any, callback: any) {
        if (!this.connected) {
            throw new Error('Not connected to WebSocket');
        }

        const subscription = this.client!.subscribe(destination, (message) => {
            try {
                const parsedMessage = JSON.parse(message.body);
                callback(parsedMessage);
            } catch (error) {
                console.error('Error parsing message:', error);
                callback(message.body);
            }
        });

        this.subscriptions.set(destination, subscription);
        return subscription;
    }

    // Room-specific action methods
    sendMessage(chatRoomId: number, content: NewMessageDto) {
        if (!this.connected) {
            throw new Error('Not connected to WebSocket');
        }

        this.client!.publish({
            destination: '/app/chat.sendMessage',
            body: JSON.stringify({
                chatRoomId: chatRoomId,
                content: content
            }),
        });
    }

    deleteMessage(messageId: number) {
        if (!this.connected) {
            throw new Error('Not connected to WebSocket');
        }

        this.client!.publish({
            destination: '/app/chat.deleteMessage',
            body: JSON.stringify({
                messageId: messageId
            }),
        });
    }

    editMessage(messageId: number, newContent: UpdateMessageDto) {
        if (!this.connected) {
            throw new Error('Not connected to WebSocket');
        }

        this.client!.publish({
            destination: '/app/chat.editMessage',
            body: JSON.stringify({
                messageId: messageId,
                newContent: newContent
            }),
        });
    }

    unsubscribe(destination: any) {
        const subscription = this.subscriptions.get(destination);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(destination);
        }
    }

    disconnect() {
        if (this.client) {
            this.subscriptions.forEach((subscription) => {
                subscription.unsubscribe();
            });
            this.subscriptions.clear();

            this.client.deactivate();
            this.connected = false;
            this.client = null;
        }
    }

    isConnected() {
        return this.connected;
    }
}

export default new WebSocketService();