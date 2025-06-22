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
import {Message} from "@/lib/models/models"
import {useWebSocket} from "@/hooks/use-web-socket";
import {useGetChatRoom} from "@/hooks/react-query";


type Variant = "received" | "sent";

const ChatInputWrapper = () => {
    return (
            <form
                className="relative rounded-lg border bg-background focus-within:ring-1 focus-within:ring-ring p-1 bg-slate-300 shadow-lg"
                    >
                    <ChatInput
                    placeholder="Type your message here..."
                    className="min-h-12 resize-none rounded-lg bg-background border-0 p-3 shadow-none focus-visible:ring-0 min-w-3xl bg-slate-300 resize-none"
                    rows={5}
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
    const [messages, setMessages] = useState<Message[]>([
        {id: 1, content: "Hello Sir", author: "John Jones", timeSent: "10:30:15.123456789"},
        {id:2, content: "Hello Mr. Jones", author: "Argel Hernandez Amaya", timeSent: "10:32:15.123456789"}
    ]);

    const author = "Argel Hernandez Amaya"

    useEffect(() => {
        if (chatRoom.data) {
            const data = chatRoom.data

            if (!(data.statusCode === 200)) {
                setIsFailed(true)
                setErrorMessage(data.data.errorMessage)
            }
        }
    })

    if (chatRoom.isLoading) {
        return (
            <div>Loading...</div>
        )
    }

    return (
        <main className={"flex flex-col items-center justify-center bg-black/10 w-full pt-2 pb-4"}>
            <ChatMessageList>
                {messages.map((message) => {
                    const variant: Variant = message.author === author ? "sent" : "received";
                    return (<ChatMessage key={message.id} variant={variant} content={message.content} author={message.author} timeSent={message.timeSent} />)
                })}
            </ChatMessageList>
            <ChatInputWrapper/>
        </main>
    )
}


