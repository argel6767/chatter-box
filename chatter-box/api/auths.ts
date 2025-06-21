import {API_VERSION_PATHING_V1, apiClient, getFailedResponse, getSuccessfulResponse} from "@/api/apiConfig";
import {
    AuthenticateUserDto,
    ChangePasswordDto,
    ForgetPasswordDto,
    RegisterUserDto,
    VerifyUserDto
} from "@/lib/models/requests";

const RESOURCE_PATH = API_VERSION_PATHING_V1 + "/auths"

export const register = async (request:RegisterUserDto) => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + "/register", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const login = async (request:AuthenticateUserDto) => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + "/login", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const verify = async (request:VerifyUserDto) => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + "/verify", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const logout = async () => {
    try {
        const response =  await apiClient.post(RESOURCE_PATH + "/logout");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const changePassword = async (request: ChangePasswordDto) => {
    try {
        const response = await apiClient.put(RESOURCE_PATH+ "/password", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const resendVerificationCode = async (username: string) => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + `/resend/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const forgotPassword = async (username: string) => {
    try {
        const response = await apiClient.post(RESOURCE_PATH + `/forgot/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const resetPassword = async (request: ForgetPasswordDto) => {
    try {
        const response = await apiClient.put(RESOURCE_PATH + "/reset");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

