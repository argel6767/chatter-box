export interface User {
    email: string,
    username: string,
    chatRooms: ChatRoomDto[],
    authDetails: AuthDetails
}

export interface ChatRoomUserDto {
    id: number,
    username: string
}

export interface ChatRoomDto {
    id: number,
    name: string
}

export interface AuthDetails {
    id: number,
    isVerified: boolean,
    roles: string[]
}

export interface EmailSentSuccessfullyDto {
    username: string,
    successMessage: string
}

export interface FailedAPIRequestResponse {
    errorMessage: string,
    instance: string
}