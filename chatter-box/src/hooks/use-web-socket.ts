import { useEffect, useState, useCallback, useRef } from 'react';
import {WebSocketService} from '@/web_socket/web-socket-service';
import { UpdateMessageDto } from "@/lib/models/requests";

export const useWebSocket = (chatRoomId: number) => {
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [subscribing, setSubscribing] = useState(false);
    const subscriptionRef = useRef<string[] | null>(null);
    const webSocket = new  WebSocketService();

    useEffect(() => {
        let mounted = true;

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
    }, [chatRoomId]);

    const subscribeToRoom = useCallback(async (callbacks: any) => {
        if (!chatRoomId || subscribing) return null;

        setSubscribing(true);
        try {
            // This now waits for connection if needed
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
    }, [chatRoomId, subscribing]);

    const sendMessage = useCallback(async (content: string) => {
        if (!chatRoomId) return;

        try {
            await webSocket.sendMessage(chatRoomId, content);
        } catch (err: any) {
            console.error('Failed to send message:', err);
            setError(err.message);
        }
    }, [chatRoomId]);

    const deleteMessage = useCallback(async (messageId: number) => {
        try {
            await webSocket.deleteMessage(messageId);
        } catch (err: any) {
            console.error('Failed to delete message:', err);
            setError(err.message);
        }
    }, []);

    const editMessage = useCallback(async (messageId: number, newContent: UpdateMessageDto) => {
        try {
            await webSocket.editMessage(messageId, newContent);
        } catch (err: any) {
            console.error('Failed to edit message:', err);
            setError(err.message);
        }
    }, []);

    return {
        connected,
        error,
        subscribeToRoom,
        sendMessage,
        deleteMessage,
        editMessage,
        subscribing,
        unsubscribe: webSocket.unsubscribeFromRoom,
    };
};