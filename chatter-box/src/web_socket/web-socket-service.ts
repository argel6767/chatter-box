import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { UpdateMessageDto} from "@/lib/models/requests";

const BASE_URL = process.env.NEXT_PUBLIC_WEBSOCKET_URL;

export class WebSocketService {
    client: Client | null;
    connected: boolean;
    subscriptions: Map<string, any>;
    baseUrl: string;
    connectionPromise: Promise<any> | null;

    constructor() {
        this.client = null;
        this.connected = false;
        this.subscriptions = new Map();
        this.baseUrl = BASE_URL!;
        this.connectionPromise = null;
    }

    connect() {
        // Return existing connection promise if already connecting/connected
        if (this.connectionPromise) {
            return this.connectionPromise;
        }

        this.connectionPromise = new Promise((resolve, reject) => {
            this.client = new Client({
                webSocketFactory: () => new SockJS(`${this.baseUrl}/ws`,undefined,
                    {
                        transports: ['websocket'],  // optional: skip polling transports
                        withCredentials: true      // ← this is the key
                    } as any),
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
                this.connectionPromise = null; // Reset on error
                reject(new Error(frame.headers['message']));
            };

            this.client.onWebSocketClose = () => {
                console.log('WebSocket connection closed');
                this.connected = false;
                this.connectionPromise = null; // Reset on close
            };

            this.client.activate();
        });

        return this.connectionPromise;
    }

    // Ensure connection before operations
    async ensureConnected() {
        if (!this.isFullyConnected()) {
            if (!this.connectionPromise) {
                await this.connect();
            } else {
                await this.connectionPromise;
            }
        }
    }

    // Room-specific subscription methods
    async subscribeToRoom(chatRoomId: number, callbacks: any) {
        await this.ensureConnected();

        if (!this.isFullyConnected()) {
            throw new Error('Failed to establish WebSocket connection');
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

    unsubscribeFromRoom(chatRoomId: number) {
        this.unsubscribe(`/topic/chat.${chatRoomId}`);
        this.unsubscribe(`/topic/chat.${chatRoomId}.delete`);
        this.unsubscribe(`/topic/chat.${chatRoomId}.edit`);
    }

    subscribe(destination: string, callback: (data: any) => void) {
        if (!this.isFullyConnected()) {
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
    async sendMessage(chatRoomId: number, content: string) {
        await this.ensureConnected();

        if (!this.isFullyConnected()) {
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

    async deleteMessage(messageId: number) {
        await this.ensureConnected();

        if (!this.isFullyConnected()) {
            throw new Error('Not connected to WebSocket');
        }

        this.client!.publish({
            destination: '/app/chat.deleteMessage',
            body: JSON.stringify({
                messageId: messageId
            }),
        });
    }

    async editMessage(messageId: number, newContent: UpdateMessageDto) {
        await this.ensureConnected();

        if (!this.isFullyConnected()) {
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

    unsubscribe(destination: string) {
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
            this.connectionPromise = null;
            this.client = null;
        }
    }

    isConnected() {
        return this.connected;
    }

    // More accurate connection check
    isFullyConnected() {
        return this.connected && this.client?.active === true;
    }
}