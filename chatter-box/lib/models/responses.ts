export interface ChatRoomUserDto {
    id: number,
    username: string
}

export interface ChatRoomDto {
    id: number,
    name: string
}

export interface DeletedChatRoomDto {
    message: string,
    statusCode: number
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

