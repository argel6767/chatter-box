'use client'
import {ChatRoomDto, FriendIdAndNameDto, FriendshipDto, Status, UserProfileDto} from "@/lib/models/responses";
import {ChatBubbleAvatar} from "@/components/ui/chat/chat-bubble";
import Link from "next/link";
import {useEffect, useState} from "react";
import {useToggle} from "@/hooks/use-toggle";
import {blockUser, sendFriendRequest, unBlockUser} from "@/api/friendship";
import {useFailedRequest} from "@/hooks/use-failed-request";
import {isFailedResponse, sleep} from "@/lib/utils";
import {Button} from "@/components/ui/button";
import {useGetUserProfile} from "@/hooks/react-query";
import {Loading} from "@/components/ui/loading";
import {AnnouncementMessage} from "@/components/ui/annoucementMessage";
import {useQueryClient} from "@tanstack/react-query";

interface FriendStatusProps {
    userId: number
    relationStatus: Status
}

export const FriendStatus = ({userId, relationStatus}: FriendStatusProps) => {
    const [relationshipStatus, setRelationshipStatus] = useState(relationStatus);
    const {value: isSendingFriendRequest, toggleValue:toggleSendingFriendRequest} = useToggle(false);
    const {value: isSendingBlockRequest, toggleValue: toggleSendingBlockRequest} = useToggle(false);
    const {failedRequest, updateFailedRequest, resetFailedRequest} = useFailedRequest();

    const handleBlock = async() => {
        toggleSendingBlockRequest();
        const response = await blockUser(userId);
        toggleSendingBlockRequest();
        if (isFailedResponse(response)) {
            updateFailedRequest(true, response.data.errorMessage)
            await sleep(2500);
            resetFailedRequest();
        }
        else {
            const data = response.data as FriendshipDto;
            setRelationshipStatus(data.status)
        }
    }

    const handleUnBlock = async() => {
        toggleSendingBlockRequest();
        const response = await unBlockUser(userId);
        toggleSendingBlockRequest();
        if (isFailedResponse(response)) {
            updateFailedRequest(true, response.data.errorMessage)
            await sleep(2500);
            resetFailedRequest();
        }
        else {
            setRelationshipStatus("NONE");
        }
    }

    const handleFriendRequest = async() => {
        toggleSendingFriendRequest();
        const response =  await sendFriendRequest(userId);
        toggleSendingFriendRequest();
        if (isFailedResponse(response)) {
            updateFailedRequest(true, response.data.errorMessage)
            await sleep(2500);
            resetFailedRequest();
        }
        else {
            const data = response.data as FriendshipDto;
            setRelationshipStatus(data.status)
        }
    }

    if (failedRequest.isFailed) {
        return (
            <main className={"text-center"}>
                <p>{failedRequest.message}</p>
            </main>
        )
    }

    return (
        <main className={"flex justify-center gap-2"}>
            {/* Friend Request Button - only show if not blocked and no existing relation */}
            {relationshipStatus !== "BLOCKED" && (
                <>
                    {relationshipStatus === "NONE" && (
                        <Button onClick={handleFriendRequest} disabled={isSendingBlockRequest || isSendingFriendRequest}>
                            {isSendingFriendRequest ? "Sending Request..."
                                : "Send Friend Request"}
                        </Button>)}
                    {/* Show status for existing relations */}
                    {relationshipStatus === "PENDING" && <p>Friend request sent</p>}
                    {relationshipStatus === "ACCEPTED" && <p>You are friends</p>}
                </>
            )}

            {/* Block/Unblock Button */}
            {relationshipStatus === "BLOCKED" ? (
                <Button onClick={handleUnBlock} disabled={isSendingBlockRequest || isSendingBlockRequest}>
                    {isSendingBlockRequest ? "Unblocking..." : "Unblock User"}
                </Button>
            ) : (
                <Button onClick={handleBlock} disabled={isSendingBlockRequest || isSendingBlockRequest}>
                    {isSendingBlockRequest ? "Blocking..." : "Block User"}
                </Button>
            )}
        </main>
    );
}

interface CommonChatRoomsDtoProps {
    chatRooms: ChatRoomDto[]
}

export const CommonChatRooms = ({chatRooms}: CommonChatRoomsDtoProps) => {

    return (
        <ul className={"flex gap-2"}>
            {chatRooms.map((chatRoom) => {
                const fallBack = chatRoom.name.substring(0,1).toUpperCase();
                return (<li key={chatRoom.id}>
                    <Link className={""} href={`/chats/room/${chatRoom.id}`}>
                        <ChatBubbleAvatar className={`bg-slate-200 text-black hover:scale-110 transition-transform duration-300`} fallback={fallBack}/>
                    </Link>
                </li>)
            })}
        </ul>
    )
}

interface CommonFriendsProps {
    friends: FriendIdAndNameDto[]
}

export const CommonFriends = ({friends} : CommonFriendsProps) => {

    return (
        <ul className={"flex gap-2"}>
            {friends.map((friend) => {
                const fallBack = friend.username.substring(0,1).toUpperCase()
                return (<li key={friend.id}>
                    <Link className={""} href={`/profiles/${friend.id}`}>
                        <ChatBubbleAvatar className={`bg-slate-200 text-black hover:scale-110 transition-transform duration-300`} fallback={fallBack}/>
                    </Link>
                </li>)
            })}
        </ul>
    )
}
interface UserInfoProps {
    id: number
}

export const UserInfo = ({id}: UserInfoProps) => {
    const profile = useGetUserProfile(id);
    const {failedRequest, updateFailedRequest, resetFailedRequest} = useFailedRequest();
    const queryClient = useQueryClient();
    const [user, setUser] = useState<UserProfileDto>({
        commonChatRooms: [],
        friends: [],
        id: 0,
        status: "NONE",
        username: ""
    })

    useEffect(() => {
        if (profile.data) {
            const response = profile.data
            if (isFailedResponse(response)) {
                updateFailedRequest(true, response.data.errorMessage)
            }
            else {
                const userData = response.data as UserProfileDto;
                setUser(userData)
            }
        }
    }, [profile.data, updateFailedRequest]);

    useEffect(() => {
        resetFailedRequest();
        queryClient.invalidateQueries({ queryKey: ["profile", id]
    });
    }, [id, queryClient, resetFailedRequest]);

    if (profile.isFetching || profile.isLoading) {
        return (
            <main className={"flex justify-center"}>
                <Loading size={"xl"}/>
            </main>
        )
    }

    if (failedRequest.isFailed) {
        return (
            <main className={"flex justify-center"}>
                <AnnouncementMessage title={"Could not fetch User's profile."} message={failedRequest.message}/>
            </main>
        )
    }

    const getFormattedUsername = () => {
        return user.username.substring(0,1).toUpperCase().concat(user.username.substring(1));
    }

    return (
        <main className={"flex flex-col justify-center gap-4"}>
            <header className={"flex flex-col jusitfy-center items-center gap-4"}>
                <ChatBubbleAvatar fallback={user.username.substring(0, 1).toUpperCase()}
                                  className={"text-5xl size-32 text-black"}/>
                <h1 className={"text-4xl"}>{getFormattedUsername()}</h1>
            </header>
            <FriendStatus userId={user.id} relationStatus={user.status}/>
            <span className={"flex justify-center gap-8"}>
                   <section className={"flex flex-col gap-2"}>
                    <h2 className={"text-2xl italic"}>Common Friends</h2>
                    <CommonFriends friends={user.friends}/>
                </section>
                <section className={"flex flex-col gap-2"}>
                    <h2 className={"text-2xl italic"}>Common ChatRooms</h2>
                    <CommonChatRooms chatRooms={user.commonChatRooms}/>
                </section>
                </span>
        </main>
    )
}