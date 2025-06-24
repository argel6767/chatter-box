import {API_VERSION_PATHING_V1, apiClient, getFailedResponse, getSuccessfulResponse} from "@/api/apiConfig";

const RESOURCE_PATH = API_VERSION_PATHING_V1 + "/friends"

export const sendFriendRequest = async (friendId: number) => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + `/request/${friendId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const acceptFriendRequest = async (friendshipId: number) => {
    try {
        const response = await apiClient.put(RESOURCE_PATH + `/accept/${friendshipId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const removeFriendship = async (friendshipId: number) => {
    try {
        const response = await apiClient.delete(RESOURCE_PATH + `/remove/${friendshipId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const blockUser = async (friendId: number) => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + `/block/${friendId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const getFriends = async () => {
    try {
        const response = await apiClient.get(RESOURCE_PATH + "/friends");
        getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const getRequests = async () => {
    try {
        const response = await apiClient.get(RESOURCE_PATH + "/requests");
        getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const getBlocked = async () => {
    try {
        const response = await apiClient.get(RESOURCE_PATH + "/blocked");
        getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}