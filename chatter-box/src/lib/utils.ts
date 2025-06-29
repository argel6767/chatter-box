import { ApiResponseWrapper, FailedAPIRequestResponse } from "@/api/apiConfig";
import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const sleep = (ms: number): Promise<void> =>
    new Promise((resolve) => setTimeout(resolve, ms));

export const isFailedResponse = <T>(response: ApiResponseWrapper<T | FailedAPIRequestResponse>): response is ApiResponseWrapper<FailedAPIRequestResponse> => {
  return response.statusCode !== 200;
}
