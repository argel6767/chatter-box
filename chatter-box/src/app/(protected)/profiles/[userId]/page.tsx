import {cookies} from "next/headers";
import {getUserProfile} from "@/api/user";
import {isFailedResponse} from "@/lib/utils";
import {AnnouncementMessage} from "@/components/ui/annoucementMessage";
import Link from "next/link";
import {UserProfileDto} from "@/lib/models/responses";
import {CommonChatRooms, CommonFriends, FriendStatus} from "@/app/(protected)/profiles/[userId]/components";
import {ChatBubbleAvatar} from "@/components/ui/chat/chat-bubble";
import {BackButton} from "@/app/auth/components";

type PageProps = {
    params: Promise<{ userId: string }>
};

export default async function UserProfile({params}: PageProps) {

    const {userId} = await params;
    const numericUserId = Number(userId);
    const cookie = await cookies();
    const cookieHeader = cookie.toString();
    const response = await getUserProfile(numericUserId, cookieHeader);

    if (isFailedResponse(response)) {
        const errorMessage = response.data.errorMessage;
        return (
            <main className={"min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900"}>
                <AnnouncementMessage title={"Could not fetch user"} message={"The user's profile you were looking for could not be fetched. Try again later"}>
                    <p>{errorMessage}</p>
                    <Link className={"text-blue-500 hover:text-blue-700 underline"} href={"/chats"}>Go Back to Chats Dashboard</Link>
                </AnnouncementMessage>
            </main>
        )
    }
    else {
        const user = response.data as UserProfileDto;
        return (
            <main className={"min-h-screen flex flex-col items-center justify-start bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-slate-300 p-4 gap-6"}>
                <BackButton path={"/chats"}/>
                <ChatBubbleAvatar fallback={user.username.substring(0,1).toUpperCase()} className={"text-5xl size-32 text-black"}/>
                <FriendStatus userId={user.id} relationStatus={user.relationshipType}/>
                <h1 className={"text-2xl"}>{user.username}</h1>
                <section className={"flex flex-col gap-2"}>
                    <h2 className={"text-xl"}>Common Friends</h2>
                    <CommonFriends friends={user.friends}/>
                </section>
                <section className={"flex flex-col gap-2"}>
                    <h2 className={"text-xl"}>Common ChatRooms</h2>
                    <CommonChatRooms chatRooms={user.commonChatRooms}/>
                </section>
            </main>
        )
    }

}