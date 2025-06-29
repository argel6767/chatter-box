
import React from 'react';
import { cn } from '@/lib/utils';

type Size = "sm" | "default" | "lg" | "xl"

interface LoadingSpinnerProps {
    size?: Size;
    className?: string;
}

// Loading Spinner Component
export const LoadingSpinner = ({ className, size = "default", ...props }: LoadingSpinnerProps) => {
    const sizeClasses = {
        sm: "h-4 w-4",
        default: "h-6 w-6",
        lg: "h-8 w-8",
        xl: "h-12 w-12"
    };

    return (
        <div
            className={cn(
                "animate-spin rounded-full border-2 border-muted border-t-primary",
                sizeClasses[size],
                className
            )}
            {...props}
        />
    );
};

interface LoadingProps {
    variant?: "spinner" | "dots" | "pulse" | "default";
    size?: Size;
    text?: string;
    className?: string;
}

export const Loading = ({variant = "default", size = "default", text, className, ...props}: LoadingProps) => {
    if (variant === "spinner") {
        return (
            <div className={cn("flex items-center justify-center", className)} {...props}>
                <LoadingSpinner size={size}  />
            </div>
        );
    }

    if (variant === "dots") {
        return (
            <div className={cn("flex items-center justify-center space-x-1", className)} {...props}>
                <div className="h-2 w-2 bg-primary rounded-full animate-bounce [animation-delay:-0.3s]"></div>
                <div className="h-2 w-2 bg-primary rounded-full animate-bounce [animation-delay:-0.15s]"></div>
                <div className="h-2 w-2 bg-primary rounded-full animate-bounce"></div>
            </div>
        );
    }

    if (variant === "pulse") {
        return (
            <div className={cn("flex items-center justify-center", className)} {...props}>
                <div className="h-4 w-4 bg-primary rounded-full animate-pulse"></div>
            </div>
        );
    }

    // Default variant with spinner and optional text
    return (
        <div className={cn("flex flex-col items-center justify-center space-y-2", className)} {...props}>
            <LoadingSpinner size={size} />
            {text && (
                <p className="text-sm text-muted-foreground animate-pulse">{text}</p>
            )}
        </div>
    );
};

interface LoadingOverlayProps {
    isLoading: boolean;
    children?: React.ReactNode;
    text?: string;
    size: Size
}

// Page-level loading overlay
export const LoadingOverlay = ({ isLoading, children, text, size}: LoadingOverlayProps) => {
    if (!isLoading) return children;

    return (
        <div className="relative">
            {children && <div className="opacity-50 pointer-events-none">{children}</div>}
            <div className="absolute inset-0 flex items-center justify-center bg-background/80 backdrop-blur-sm">
                <Loading text={text} size={size} />
            </div>
        </div>
    );
};