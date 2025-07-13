'use client'
import {ChatRoomDto, FriendIdAndNameDto, FriendshipDto, Status} from "@/lib/models/responses";
import {ChatBubbleAvatar} from "@/components/ui/chat/chat-bubble";
import Link from "next/link";
import {useState} from "react";
import {useToggle} from "@/hooks/use-toggle";
import {blockUser, sendFriendRequest, unBlockUser} from "@/api/friendship";
import {useFailedRequest} from "@/hooks/use-failed-request";
import {isFailedResponse, sleep} from "@/lib/utils";
import {Button} from "@/components/ui/button";

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
            const data = response.data as FriendshipDto;
            setRelationshipStatus(data.status);
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
            <main>
                <p>{failedRequest.message}</p>
            </main>
        )
    }

    return (
        <main>
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