import {Card, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";

interface AnnouncementMessageProps {
    message?: string;
    title: string;
    children?: React.ReactNode;
}
export const AnnouncementMessage = ({message, title, children}: AnnouncementMessageProps) => {
    return (
        <main className="motion-preset-fade motion-duration-700 w-full">
            <Card className="bg-black/20 backdrop-blur-sm border-white/10">
                <CardHeader>
                    <CardTitle className="text-white text-center text-xl pb-3">
                        {title}
                    </CardTitle>
                    <CardDescription className="text-gray-400 text-center flex flex-col items-center justify-center w-full gap-4">
                        {message}
                        {children}
                    </CardDescription>
                </CardHeader>
            </Card>
        </main>
    )
}