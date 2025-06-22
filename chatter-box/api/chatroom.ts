import {
    API_VERSION_PATHING_V1,
    apiClient,
    ApiResponseWrapperPromise, FailedAPIRequestResponse,
    getFailedResponse,
    getSuccessfulResponse
} from "@/api/apiConfig";
import {NewChatDto} from "@/lib/models/requests";
import {ChatRoom} from "@/lib/models/models";
import {DeletedChatRoomDto} from "@/lib/models/responses";

const RESOURCE_PATH = API_VERSION_PATHING_V1 + "/chats";

export const createChatRoom = async (request: NewChatDto): ApiResponseWrapperPromise<ChatRoom, FailedAPIRequestResponse> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH, request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const getChatRoom = async (id: number): ApiResponseWrapperPromise<ChatRoom, FailedAPIRequestResponse> => {
    try {
        const response = await apiClient.get(RESOURCE_PATH + `/${id}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        getFailedResponse(error);
    }
}

export const deleteChatRoom = async (id: number): ApiResponseWrapperPromise<DeletedChatRoomDto, FailedAPIRequestResponse> => {
    try {
        const response = await apiClient.delete(RESOURCE_PATH + `/${id}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        getFailedResponse(error);
    }
}

export const addMemberToChat = async (chatRoomId: number, username: string): ApiResponseWrapperPromise<ChatRoom, FailedAPIRequestResponse> => {
    try {
        const response = await apiClient.put(RESOURCE_PATH + `/${chatRoomId}/members/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        getFailedResponse(error);
    }
}

export const removeMemberFromChat = async (chatRoomId: number, username: string): ApiResponseWrapperPromise<ChatRoom, FailedAPIRequestResponse> => {
    try {
        const response = await apiClient.delete(RESOURCE_PATH + `/${chatRoomId}/members/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        getFailedResponse(error);
    }
}

export const leaveChatRoom = async (chatRoomId: number): ApiResponseWrapperPromise<DeletedChatRoomDto, FailedAPIRequestResponse> => {
    try {
        const response = await apiClient.delete(RESOURCE_PATH + `/${chatRoomId}/members/me`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        getFailedResponse(error);
    }
}