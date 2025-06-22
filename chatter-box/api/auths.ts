import {
    API_VERSION_PATHING_V1,
    apiClient, ApiResponseWrapper,
    FailedAPIRequestResponse,
    getFailedResponse,
    getSuccessfulResponse
} from "@/api/apiConfig";
import {
    AuthenticateUserDto,
    ChangePasswordDto,
    ForgetPasswordDto,
    RegisterUserDto,
    VerifyUserDto
} from "@/lib/models/requests";
import {User} from "@/lib/models/models";
import {EmailSentSuccessfullyDto} from "@/lib/models/responses";

const RESOURCE_PATH = API_VERSION_PATHING_V1 + "/auths"

export const register = async (request:RegisterUserDto): Promise<ApiResponseWrapper<User | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + "/register", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const login = async (request:AuthenticateUserDto): Promise<ApiResponseWrapper<User | FailedAPIRequestResponse>> => {
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
        const response = await apiClient.post(RESOURCE_PATH + `/resend/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const forgotPassword = async (username: string): Promise<ApiResponseWrapper<EmailSentSuccessfullyDto | FailedAPIRequestResponse>> => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + `/forgot/${username}`);
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

