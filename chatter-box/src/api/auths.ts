import {API_VERSION_PATHING_V1, API_VERSION_PATHING_V2, apiClient, ApiResponseWrapper, FailedAPIRequestResponse, getFailedResponse, getSuccessfulResponse} from "@/api/apiConfig";
import {AuthenticateUserDto, ChangePasswordDto, ForgetPasswordDto, RegisterUserDto, VerifyUserDto} from "@/lib/models/requests";
import {User} from "@/lib/models/models";
import {EmailSentSuccessfullyDto} from "@/lib/models/responses";

const RESOURCE_PATH = API_VERSION_PATHING_V1 + "/auths"
const RESOURCE_PATH_V2 = API_VERSION_PATHING_V2 + "/auths"

export const checkCookie = async(): Promise<ApiResponseWrapper<string | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + "/cookie-status");
        return getSuccessfulResponse(response);
    }
    catch(error) {
        return getFailedResponse(error);
    }
}

export const register = async (request:RegisterUserDto): Promise<ApiResponseWrapper<string | FailedAPIRequestResponse>> => {
    const lowercaseUsername = request.username.toLowerCase();
    request.username = lowercaseUsername
    const lowercaseEmail = request.email.toLowerCase();
    request.email = lowercaseEmail;
    try {
        const response = await apiClient.post(RESOURCE_PATH_V2 + "/register", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const login = async (request:AuthenticateUserDto): Promise<ApiResponseWrapper<User | FailedAPIRequestResponse>> => {
    const lowercase = request.username.toLowerCase();
    request.username = lowercase
    try {
        const response = await apiClient.post(RESOURCE_PATH + "/login", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const verify = async (request:VerifyUserDto): Promise<ApiResponseWrapper<string | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + "/verify", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const logout = async (): Promise<ApiResponseWrapper<void | FailedAPIRequestResponse>> => {
    try {
        const response =  await apiClient.post(RESOURCE_PATH + "/logout");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const changePassword = async (request: ChangePasswordDto): Promise<ApiResponseWrapper<User | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.put(RESOURCE_PATH+ "/password", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const resendVerificationCode = async (username: string): Promise<ApiResponseWrapper<EmailSentSuccessfullyDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH_V2 + `/resend-verification/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const forgotPassword = async (username: string): Promise<ApiResponseWrapper<EmailSentSuccessfullyDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH_V2 + `/forgot/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const resetPassword = async (request: ForgetPasswordDto): Promise<ApiResponseWrapper<User | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.put(RESOURCE_PATH + "/reset", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

