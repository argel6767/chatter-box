import {API_VERSION_PATHING_V1, apiClient, ApiResponseWrapper, FailedAPIRequestResponse, getFailedResponse, getSuccessfulResponse} from "@/api/apiConfig";
import { FriendshipDto } from "@/lib/models/responses";

const RESOURCE_PATH = API_VERSION_PATHING_V1 + "/friends"

export const sendFriendRequest = async (friendId: number): Promise<ApiResponseWrapper<FriendshipDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + `/request/${friendId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const acceptFriendRequest = async (friendshipId: number): Promise<ApiResponseWrapper<FriendshipDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.put(RESOURCE_PATH + `/accept/${friendshipId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const removeFriendship = async (friendshipId: number): Promise<ApiResponseWrapper<FriendshipDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.delete(RESOURCE_PATH + `/remove/${friendshipId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const blockUser = async (friendId: number): Promise<ApiResponseWrapper<FriendshipDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + `/block/${friendId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const unBlockUser = async (friendId: number): Promise<ApiResponseWrapper<FriendshipDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + `/un-block/${friendId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const getFriends = async (): Promise<ApiResponseWrapper<FriendshipDto[] | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.get(RESOURCE_PATH);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const getRequests = async (): Promise<ApiResponseWrapper<FriendshipDto[] | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.get(RESOURCE_PATH + "/requests");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const getBlocked = async (): Promise<ApiResponseWrapper<FriendshipDto[] | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.get(RESOURCE_PATH + "/blocked");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}