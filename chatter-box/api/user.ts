import {API_VERSION_PATHING_V1, apiClient, getFailedResponse, getSuccessfulResponse} from "@/api/apiConfig";

const RESOURCE_PATH = API_VERSION_PATHING_V1 + "/users"

export const getAccountDetails = async () => {
    try {
        const response =  await apiClient.get(RESOURCE_PATH + "/me");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const deleteAccount = async () => {
    try {
        const response =  await apiClient.delete(RESOURCE_PATH + "/me");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}