import axios from 'axios';

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

export const createResponseWrapper = <T>(data: T, statusCode: number): ApiResponseWrapper<T> => {
    return {
        statusCode: statusCode,
        data: data
    }
}

export interface FailedAPIRequestResponse {
    errorMessage: string,
    instance: string
}
 