'use client'
import {ChatMessageList} from "@/components/ui/chat/chat-message-list";
import {
    ChatBubble, ChatBubbleAction,
    ChatBubbleActionWrapper,
    ChatBubbleAvatar,
    ChatBubbleMessage
} from "@/components/ui/chat/chat-bubble";
import {useEffect, useState} from "react";
import {Label} from "@/components/ui/label";
import {CornerDownLeft, Edit, Paperclip, Trash} from "lucide-react";
import {ChatInput} from "@/components/ui/chat/chat-input";
import {Button} from "@/components/ui/button";
import {ChatRoom, Message} from "@/lib/models/models"
import {useWebSocket} from "@/hooks/use-web-socket";
import {useGetChatRoom} from "@/hooks/react-query";
import {ApiResponseWrapper, FailedAPIRequestResponse} from "@/api/apiConfig";
import {useUserStore} from "@/hooks/stores";



type Variant = "received" | "sent";

interface ChatInputWrapperProps {
    id: number
}

const ChatInputWrapper = ({id}: ChatInputWrapperProps) => {
    const {sendMessage} = useWebSocket(id);
    const [message, setMessage] = useState("");


    const handleSendMessage = () => {
        sendMessage(message.trim());
        setMessage("");
    }


    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSendMessage();
        }
    };

    const handleClickSend = (e) => {
        e.preventDefault();
        handleSendMessage();
    }

    return (
            <form onSubmit={handleClickSend} onKeyDownCapture={handleKeyPress}
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
    timeSent: string

}

const ChatMessage = ({variant, content, author, timeSent}:ChatMessageProps) => {
    const fallBack = author.substring(0,1).toUpperCase();
    const textAlignment = variant === "sent"? "text-right" : "text-left";
    const actionIcons = [
        { icon: Trash, type: "Delete" },
        {icon: Edit, type: "Edit"},
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
            <ChatBubbleActionWrapper>
                {actionIcons.map(({ icon: Icon, type }, index) => (
                    <ChatBubbleAction
                        className="size-7"
                        key={type}
                        icon={<Icon className="size-4 text-slate-200" />}
                        onClick={() =>
                            console.log(
                                "Action " + type + " clicked for message " + index,
                            )
                        }
                        variant={"bubbleAction"}
                    />
                ))}
            </ChatBubbleActionWrapper>
        </ChatBubble>
    )
}

interface ChatContainerProps {
    id: number
}

export const ChatContainer = ({id}: ChatContainerProps) => {
    const webSocket = useWebSocket(id)
    const chatRoom = useGetChatRoom(id);
    const [isFailed, setIsFailed] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [chatRoomDetails, setChatRoomDetails] = useState<ChatRoom>({creator: "", id: 0, members: [], messages: [], name: ""});
    const {user} = useUserStore();

    useEffect(() => {
        if (chatRoom.data) {
            const data = chatRoom.data
            const isFailedResponse = (response: ApiResponseWrapper<ChatRoom | FailedAPIRequestResponse>): response is ApiResponseWrapper<FailedAPIRequestResponse> => {
                return response.statusCode !== 200;
            }

            if (isFailedResponse(data)) {
                setIsFailed(true)
                setErrorMessage(data.data.errorMessage)
            }
            else {
                const chatRoom = data.data as ChatRoom;
                setChatRoomDetails(chatRoom);
            }
        }
    }, [chatRoom.data])

    useEffect(() => {
        if (webSocket.connected && id) {
            webSocket.subscribeToRoom({
                onMessage: (message: Message) => {
                    console.log('New message received:', message);
                    setChatRoomDetails((prev: ChatRoom) => {
                        return {
                            ...prev,
                            messages: [...prev.messages, message as Message]
                        }
                    });
                },

                onDelete: (messageId: number) => {
                    console.log('Message deleted:', messageId);
                    setChatRoomDetails((prev: ChatRoom) => {
                        return {
                            ...prev,
                            messages: prev.messages.filter(message => message.id !== messageId)
                        }
                    });
                },

                onEdit: (editedMessage: Message) => {
                    console.log('Message edited:', editedMessage);
                    setChatRoomDetails((prev: ChatRoom) => {
                        return {
                            ...prev,
                            messages: prev.messages.map(message => message.id === editedMessage.id ? editedMessage : message)
                        }
                        }
                    );
                }
            });
        }
    }, [webSocket, id,]);

    if (chatRoom.isLoading || !webSocket.connected) {
        return (
            <div>Loading...</div>
        )
    }

    if (isFailed || webSocket.error) {
        const error = errorMessage.length > 0 ? errorMessage : "Connection could not be established."
        return (
            <div className={"text-center text-red-500 flex flex-col items-center justify-center h-full"}>
                <h1 className={"text-2xl"}>Failed to load chat room</h1>
                <p className={"text-1xl"}>{error}</p>
                <Button variant={"default"} >Try Again</Button>
            </div>
        )
    }

    return (
        <main className={"flex flex-col items-center justify-center bg-black/10 w-full pt-2 pb-4"}>
            <h1 className={"text-2xl font-bold text-slate-200"}>{chatRoomDetails.name}</h1>
            <ChatMessageList>
                {chatRoomDetails.messages.map((message) => {
                    const variant: Variant = message.author ===  user.username? "sent" : "received";
                    return (<ChatMessage key={message.id} variant={variant} content={message.content} author={message.author} timeSent={message.timeSent} />)
                })}
            </ChatMessageList>
            <ChatInputWrapper id={id}/>
        </main>
    )
}


