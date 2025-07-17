import { useEffect, useState, useCallback, useRef } from 'react';
import {WebSocketService} from '@/web_socket/web-socket-service';
import { UpdateMessageDto } from "@/lib/models/requests";

export const useWebSocket = (chatRoomId: number) => {
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [subscribing, setSubscribing] = useState(false);
    const subscriptionRef = useRef<string[] | null>(null);

    const webSocketRef = useRef<WebSocketService | null>(null);

    const getWebSocket = useCallback(() => {
        if (!webSocketRef.current) {
            webSocketRef.current = new WebSocketService();
        }
        return webSocketRef.current;
    }, []);

    useEffect(() => {
        let mounted = true;
        const webSocket = getWebSocket();

        const connect = async () => {
            try {
                await webSocket.connect();
                if (mounted) {
                    setConnected(true);
                    setError(null);
                }
            } catch (err: any) {
                if (mounted) {
                    setError(err.message);
                    setConnected(false);
                }
            }
        };

        connect();

        return () => {
            mounted = false;
            if (chatRoomId && subscriptionRef.current) {
                webSocket.unsubscribeFromRoom(chatRoomId);
            }
        };
    }, [chatRoomId, getWebSocket]);

    const subscribeToRoom = useCallback(async (callbacks: any) => {
        if (!chatRoomId || subscribing) return null;

        setSubscribing(true);
        try {
            // This now waits for connection if needed
            const webSocket = getWebSocket();
            const keys = await webSocket.subscribeToRoom(chatRoomId, callbacks);
            subscriptionRef.current = keys;
            return keys;
        } catch (err: any) {
            console.error('Failed to subscribe to room:', err);
            setError(err.message);
            return null;
        } finally {
            setSubscribing(false);
        }
    }, [chatRoomId, subscribing, getWebSocket]);

    const sendMessage = useCallback(async (content: string) => {
        if (!chatRoomId) return;

        try {
            const webSocket = getWebSocket();
            await webSocket.sendMessage(chatRoomId, content);
        } catch (err: any) {
            console.error('Failed to send message:', err);
            setError(err.message);
        }
    }, [chatRoomId, getWebSocket]);

    const deleteMessage = useCallback(async (messageId: number) => {
        try {
            const webSocket = getWebSocket();
            await webSocket.deleteMessage(messageId);
        } catch (err: any) {
            console.error('Failed to delete message:', err);
            setError(err.message);
        }
    }, [getWebSocket]);

    const editMessage = useCallback(async (messageId: number, newContent: UpdateMessageDto) => {
        try {
            const webSocket = getWebSocket();
            await webSocket.editMessage(messageId, newContent);
        } catch (err: any) {
            console.error('Failed to edit message:', err);
            setError(err.message);
        }
    }, [getWebSocket]);

    const unsubscribe = useCallback((roomId: number) => {
        const webSocket = getWebSocket();
        webSocket.unsubscribeFromRoom(roomId);
    }, [getWebSocket]);

    return {
        connected,
        error,
        subscribeToRoom,
        sendMessage,
        deleteMessage,
        editMessage,
        subscribing,
        unsubscribe
    };
};