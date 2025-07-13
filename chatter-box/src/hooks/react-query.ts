import {QueryClient, useQuery} from "@tanstack/react-query";
import {getChatRoom} from "@/api/chatroom";
import {getUserProfile, queryUsers} from "@/api/user";
import { getFriends, getRequests } from "@/api/friendship";

export const useGetChatRoom = (chatRoomId: number) => {
    return useQuery({
        queryKey: ["chats", chatRoomId],
        queryFn: async () => {
            return await getChatRoom(chatRoomId);
        },
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

export const useGetFriends = (userId: number) => {
    return useQuery({
        queryKey: ["friends", userId],
        queryFn: async () => {
            return await getFriends();
        }
    })
}

export const useGetFriendRequests = (userId: number) => {
    return useQuery({
        queryKey: ["friendRequests", userId],
        queryFn: async () => {
            return await getRequests();
        }
    })
}

export const useGetUserQuery = (query: string) => {
    return useQuery({
        queryKey: ["userQuery", query],
        queryFn: async () => {
            return await queryUsers(query);
        },
        enabled: !!query
    })
}

export const queryClient = new QueryClient({
    defaultOptions: {
        queries : {
            refetchOnWindowFocus: false,
            staleTime: 10 * 60 * 1000, //10 minutes
        }
    }
})