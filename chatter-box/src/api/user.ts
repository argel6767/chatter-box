import {API_VERSION_PATHING_V1, apiClient, ApiResponseWrapper, FailedAPIRequestResponse, getFailedResponse, getSuccessfulResponse} from "@/api/apiConfig";
import { User } from "@/lib/models/models";
import { QueriedUserDto, UserProfileDto } from "@/lib/models/responses";

const RESOURCE_PATH = API_VERSION_PATHING_V1 + "/users"

export const getAccountDetails = async (): Promise<ApiResponseWrapper<User | FailedAPIRequestResponse>> => {
    try {
        const response =  await apiClient.get(RESOURCE_PATH + "/me");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const deleteAccount = async (): Promise<ApiResponseWrapper<VoidFunction | FailedAPIRequestResponse>> => {
    try {
        const response =  await apiClient.delete(RESOURCE_PATH + "/me");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const queryUsers= async(query: string): Promise<ApiResponseWrapper<QueriedUserDto[] | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.get(RESOURCE_PATH + "/query", {
            params: {
                query: query
            }
        })
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const getUserProfile = async (searchedUserId: number): Promise<ApiResponseWrapper<UserProfileDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.get(RESOURCE_PATH + `${searchedUserId}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}