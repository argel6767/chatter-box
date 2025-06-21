// components/ui/chat/chat-input.tsx
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";
import React from "react";

interface ChatInputProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement>{}

const ChatInput = React.forwardRef<HTMLTextAreaElement, ChatInputProps>(
    ({ className, ...props }, ref) => (
        <Textarea
            autoComplete="off"
            ref={ref}
            name="message"
            className={cn(
                "resize-none bg-slate-200 h-16 px-4 py-3 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50 w-full rounded-md overflow-y-auto",
                className,
            )}
            rows={3} // Reduced from 5 to better fit h-16
            {...props}
        />
    ),
);
ChatInput.displayName = "ChatInput";

export { ChatInput };