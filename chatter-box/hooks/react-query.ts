import {useQuery} from "@tanstack/react-query";
import {getChatRoom} from "@/api/chatroom";
import {getUserProfile} from "@/api/user";

export const useGetChatRoom = (chatRoomId: number) => {
    return useQuery({
        queryKey: ["chats", chatRoomId],
        queryFn: async () => {
            return await getChatRoom(chatRoomId);
        }
    })
}

export const useGetUserProfile = (searchedUserId: number) => {
    return useQuery({
        queryKey: ["user", searchedUserId],
        queryFn: async () => {
            return await getUserProfile(searchedUserId);
        }
    })
}