// hooks/useWebSocket.js
import { useEffect, useState, useCallback } from 'react';
import WebSocketService from '../web_socket/web-socket-service';
import {NewMessageDto, UpdateMessageDto} from "@/lib/models/requests";

export const useWebSocket = (chatRoomId: number) => {
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const connect = async () => {
            try {
                await WebSocketService.connect();
                setConnected(true);
                setError(null);
            } catch (err) {
                setError(err.message);
                setConnected(false);
            }
        };

        connect();

        // Note: We don't disconnect on unmount because other components might be using it
        return () => {
            if (chatRoomId) {
                WebSocketService.unsubscribeFromRoom(chatRoomId);
            }
        };
    }, [chatRoomId]);

    const subscribeToRoom = useCallback((callbacks) => {
        if (!connected || !chatRoomId) return null;

        return WebSocketService.subscribeToRoom(chatRoomId, callbacks);
    }, [connected, chatRoomId]);

    const sendMessage = useCallback((content: NewMessageDto) => {
        if (!connected || !chatRoomId) return;
        WebSocketService.sendMessage(chatRoomId, content);
    }, [connected, chatRoomId]);

    const deleteMessage = useCallback((messageId: number) => {
        if (!connected) return;
        WebSocketService.deleteMessage(messageId);
    }, [connected]);

    const editMessage = useCallback((messageId: number, newContent: UpdateMessageDto) => {
        if (!connected) return;
        WebSocketService.editMessage(messageId, newContent);
    }, [connected]);

    return {
        connected,
        error,
        subscribeToRoom,
        sendMessage,
        deleteMessage,
        editMessage,
    };
};