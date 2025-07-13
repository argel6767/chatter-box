'use client'
import {ChatMessageList} from "@/components/ui/chat/chat-message-list";
import {
    ChatBubble, ChatBubbleAction,
    ChatBubbleActionWrapper,
    ChatBubbleAvatar,
    ChatBubbleMessage
} from "@/components/ui/chat/chat-bubble";
import {useCallback, useEffect, useState, useRef} from "react";
import {Label} from "@/components/ui/label";
import {CornerDownLeft, Edit, Paperclip, Trash} from "lucide-react";
import {ChatInput} from "@/components/ui/chat/chat-input";
import {Button} from "@/components/ui/button";
import {ChatRoom, Message} from "@/lib/models/models"
import {useWebSocket} from "@/hooks/use-web-socket";
import {useGetChatRoom} from "@/hooks/react-query";
import {useUserStore} from "@/hooks/stores";
import {AnnouncementMessage} from "@/components/ui/annoucementMessage";
import {LoadingSpinner} from "@/components/ui/loading";
import {useRouter} from "next/navigation";
import { isFailedResponse } from "@/lib/utils";
import { useQueryClient } from "@tanstack/react-query";

type Variant = "received" | "sent";

interface ChatInputWrapperProps {
    id: number
}

const ChatInputWrapper = ({id}: ChatInputWrapperProps) => {
    const {sendMessage, connected} = useWebSocket(id);
    const [message, setMessage] = useState("");

    const handleSendMessage = async () => {
        const trimmedMessage = message.trim();
        if (!trimmedMessage) return;

        try {
            await sendMessage(trimmedMessage);
            setMessage("");
        } catch (err) {
            console.error('Failed to send message:', err);
        }
    }

    const handleKeyPress = async(e: React.KeyboardEvent) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            await handleSendMessage();
        }
    };

    const handleClickSend = async (e: React.FormEvent) => {
        e.preventDefault();
        await handleSendMessage();
    }

    return (
        <form onSubmit={handleClickSend} onKeyDown={handleKeyPress}
              className="relative rounded-lg border bg-background focus-within:ring-1 focus-within:ring-ring p-1 bg-slate-300 shadow-lg"
        >
            <ChatInput
                placeholder="Type your message here..."
                className="min-h-12 resize-none rounded-lg bg-background border-0 p-3 shadow-none focus-visible:ring-0 min-w-3xl bg-slate-300 resize-none"
                rows={5}
                value={message}
                onChange={(e) => setMessage(e.target.value)}
            />
            <div className="flex items-center p-3 pt-0">
                <Button variant="ghost" size="icon">
                    <Paperclip className="size-4" />
                    <span className="sr-only">Attach file</span>
                </Button>
                <Button
                    size="sm"
                    className="ml-auto gap-1.5"
                    disabled={!connected || !message.trim()}
                >
                    Send Message
                    <CornerDownLeft className="size-3.5" />
                </Button>
            </div>
        </form>
    )
}

interface ChatMessageProps {
    variant: Variant,
    content: string,
    author: string,
    timeSent: string,
    onDelete?: () => void,
    onEdit?: () => void
}

const ChatMessage = ({variant, content, author, timeSent, onDelete, onEdit}: ChatMessageProps) => {
    const fallBack = author.substring(0,1).toUpperCase();
    const textAlignment = variant === "sent"? "text-right" : "text-left";
    const actionIcons = [
        { icon: Trash, type: "Delete", action: onDelete },
        { icon: Edit, type: "Edit", action: onEdit },
    ];

    return (
        <ChatBubble variant={variant}>
            <ChatBubbleAvatar fallback={fallBack} className={"bg-slate-200"}/>
            <ChatBubbleMessage variant={variant} className={textAlignment}>
                {content}
                <section className={"flex justify-between gap-2 pt-2 text-xs"}>
                    <Label>{author}</Label>
                    <Label>{timeSent}</Label>
                </section>
            </ChatBubbleMessage>
            {variant === "sent" && (
                <ChatBubbleActionWrapper>
                    {actionIcons.map(({ icon: Icon, type, action }) => (
                        <ChatBubbleAction
                            className="size-7"
                            key={type}
                            icon={<Icon className="size-4 text-slate-200" />}
                            onClick={action}
                            variant={"bubbleAction"}
                        />
                    ))}
                </ChatBubbleActionWrapper>
            )}
        </ChatBubble>
    )
}

interface ChatContainerProps {
    id: number
}

export const ChatContainer = ({id}: ChatContainerProps) => {
    const chatRoom = useGetChatRoom(id);
    const queryClient = useQueryClient();
    const webSocket = useWebSocket(id);
    const [isFailed, setIsFailed] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [chatRoomDetails, setChatRoomDetails] = useState<ChatRoom>({
        creator: "",
        id: 0,
        members: [],
        messages: [],
        name: ""
    });
    const {user} = useUserStore();
    const [subscribed, setSubscribed] = useState(false);
    const subscriptionAttemptRef = useRef(false);
    const router = useRouter();

    // Process chat room data
    useEffect(() => {
        if (chatRoom.data) {
            const data = chatRoom.data;

            if (isFailedResponse(data)) {
                setIsFailed(true);
                setErrorMessage(data.data.errorMessage);
            } else {
                const chatRoomData = data.data as ChatRoom;
                setChatRoomDetails(chatRoomData);
            }
        }
    }, [chatRoom.data]);

    useEffect(() => {
        setChatRoomDetails({
            creator: "",
            id: 0,
            members: [],
            messages: [],
            name: ""
        });
        setIsFailed(false);
        setErrorMessage("");
        setSubscribed(false);
        subscriptionAttemptRef.current = false;
        queryClient.invalidateQueries({queryKey:["chats", id]});
    }, [id, queryClient]);

    // Message handlers
    const handleMessage = useCallback((message: Message) => {
        setChatRoomDetails((prev: ChatRoom) => ({
            ...prev,
            messages: [...prev.messages, message],
        }));
    }, []);

    const handleDelete = useCallback((messageId: number) => {
        setChatRoomDetails((prev: ChatRoom) => ({
            ...prev,
            messages: prev.messages.filter((message) => message.id !== messageId),
        }));
    }, []);

    const handleEdit = useCallback((editedMessage: Message) => {
        setChatRoomDetails((prev: ChatRoom) => ({
            ...prev,
            messages: prev.messages.map((message) =>
                message.id === editedMessage.id ? editedMessage : message
            ),
        }));
    }, []);

    // Handle WebSocket subscriptions
    useEffect(() => {
        if (!webSocket.connected || !id || subscribed || subscriptionAttemptRef.current) {
            return;
        }

        subscriptionAttemptRef.current = true;

        const setupSubscriptions = async () => {
            try {
                const result = await webSocket.subscribeToRoom({
                    onMessage: handleMessage,
                    onDelete: handleDelete,
                    onEdit: handleEdit,
                });

                if (result) {
                    setSubscribed(true);
                    console.log('Successfully subscribed to room:', id);
                }
            } catch (err) {
                console.error('Failed to subscribe to room:', err);
                subscriptionAttemptRef.current = false;
            }
        };

        setupSubscriptions();

        return () => {
            if (subscribed) {
                webSocket.unsubscribe(id);
                setSubscribed(false);
                subscriptionAttemptRef.current = false;
            }
        };
    }, [webSocket, id, subscribed, handleMessage, handleDelete, handleEdit]);

    // Delete message handler
    const handleDeleteMessage = useCallback(async (messageId: number) => {
        try {
            await webSocket.deleteMessage(messageId);
        } catch (err) {
            console.error('Failed to delete message:', err);
        }
    }, [webSocket]);

    // Edit message handler (placeholder)
    const handleEditMessage = useCallback(async (messageId: number) => {
        // TODO: Implement edit functionality
        console.log('Edit message:', messageId);
    }, []);

    if ((chatRoom.isLoading && !subscribed && !webSocket.connected)) {
        return (
            <main className={"flex flex-col items-center justify-center w-full pt-2 pb-4 h-full shadow-lg"}>
                <AnnouncementMessage title={"Connecting to chat room..."}>
                    <LoadingSpinner size={"xl"}/>
                </AnnouncementMessage>
            </main>
        );
    }

    const isNotAMember = () => {
        return errorMessage.includes("You are not a member")
    };

    if (isFailed || webSocket.error) {
        const error = errorMessage || webSocket.error || "Connection could not be established.";
        return (
            <main className={"flex flex-col items-center justify-center w-full pt-2 pb-4 h-full shadow-lg"}>
                <AnnouncementMessage title={"Something went wrong."} message={error}>
                    {isNotAMember() ? <Button variant={"default"} onClick={() => router.replace("/chats")}>Go Back</Button> :
                        <Button variant={"default"} onClick={() => window.location.reload()}>Try Again</Button>}
                </AnnouncementMessage>
            </main>
        );
    }

    return (
        <main className={"flex flex-col items-center justify-center bg-black/10 w-full pt-2 pb-4 max-h-screen min-h-screen shadow-lg"}>
                <h1 className={"text-5xl font-bold text-slate-200 pt-2"}>{chatRoomDetails.name}</h1>
                <div className="max-h-5/6 overflow-y-scroll w-full py-2">
                <ChatMessageList>
                    {chatRoomDetails.messages.map((message) => {
                        const variant: Variant = message.author === user.username ? "sent" : "received";
                        return (
                            <ChatMessage
                                key={message.id}
                                variant={variant}
                                content={message.content}
                                author={message.author}
                                timeSent={message.timeSent}
                                onDelete={variant === "sent" ? () => handleDeleteMessage(message.id) : undefined}
                                onEdit={variant === "sent" ? () => handleEditMessage(message.id) : undefined}
                            />
                        );
                    })}
                </ChatMessageList>
            </div>
            <span className="max-h-1/6 mt-auto">
                <ChatInputWrapper id={id}/>
            </span>
        </main>
    );
};