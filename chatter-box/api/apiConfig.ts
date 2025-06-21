import axios, {AxiosError, AxiosResponse} from 'axios';
import {User} from "@/lib/models/models";

export const apiClient = axios.create({
    baseURL: process.env.NEXT_PUBLIC_BACKEND_API_BASE_URL,
    // Add this line to ensure cookies are sent with cross-origin requests
    withCredentials: true
});



export const failedCallMessage = (error: any): string => {
    return `Something went wrong and the api call failed: ${error}`
};

export const API_VERSION_PATHING_V1 = "/api/v1";

export interface ApiResponseWrapper<T> {
    statusCode: number,
    data: T
}

const createResponseWrapper = <T>(data: T, statusCode: number): ApiResponseWrapper<T> => {
    return {
        statusCode: statusCode,
        data: data
    }
}

interface FailedAPIRequestResponse {
    errorMessage: string,
    instance: string
}

export const getFailedResponse = (error: unknown) => {
    if (!(error instanceof AxiosError)) {
        throw error;
    }
    const errorBody = error?.response?.data;
    const statusCode = error?.status;
    console.error(failedCallMessage(error));
    return createResponseWrapper<FailedAPIRequestResponse>(errorBody, statusCode!);
}

export const getSuccessfulResponse = (response: AxiosResponse<any, any>) => {
    return createResponseWrapper<User>(response.data, response.status);
}
 