import {API_VERSION_PATHING_V1, apiClient, createResponseWrapper, FailedAPIRequestResponse} from "@/api/apiConfig";
import {
    AuthenticateUserDto,
    ChangePasswordDto,
    ForgetPasswordDto,
    RegisterUserDto,
    VerifyUserDto
} from "@/lib/models/requests";
import {AxiosError, AxiosResponse} from "axios";
import {User} from "@/lib/models/models";

const MODULE_PATH = API_VERSION_PATHING_V1 + "/auths"

export const register = async (request:RegisterUserDto) => {
    try {
        const response = await apiClient.post(MODULE_PATH + "/register", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const login = async (request:AuthenticateUserDto) => {
    try {
        const response = await apiClient.post(MODULE_PATH + "/login", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const verify = async (request:VerifyUserDto) => {
    try {
        const response = await apiClient.post(MODULE_PATH + "/verify", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const logout = async () => {
    try {
        const response =  await apiClient.post(MODULE_PATH + "/logout");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const changePassword = async (request: ChangePasswordDto) => {
    try {
        const response = await apiClient.put(MODULE_PATH+ "/password", request);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const resendVerificationCode = async (username: string) => {
    try {
        const response = await apiClient.post(MODULE_PATH + `/resend/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const forgotPassword = async (username: string) => {
    try {
        const response = await apiClient.post(MODULE_PATH + `/forgot/${username}`);
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

export const resetPassword = async (request: ForgetPasswordDto) => {
    try {
        const response = await apiClient.put(MODULE_PATH + "/reset");
        return getSuccessfulResponse(response);
    }
    catch (error) {
        return getFailedResponse(error);
    }
}

const getFailedResponse = (error: unknown) => {
    if (!(error instanceof AxiosError)) {
        throw error;
    }
    const errorBody = error?.response?.data;
    const statusCode = error?.status;
    return createResponseWrapper<FailedAPIRequestResponse>(errorBody, statusCode!);
}

const getSuccessfulResponse = (response: AxiosResponse<any, any>) => {
    return createResponseWrapper<User>(response.data, response.status);
}