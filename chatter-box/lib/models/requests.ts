/**
 * Auth Dtos
 */
export interface AuthenticateUserDto {
    username: string,
    password: string
}

export interface ChangePasswordDto {
    username: string,
    oldPassword: string,
    newPassword: string
}

export interface ForgetPasswordDto {
    username: string,
    newPassword: string,
    verificationCode: string
}

export interface RegisterUserDto {
    email: string,
    username: string,
    password: string
}

export interface VerifyUserDto {
    email: string,
    username: string
    code: string
}

/**
 * Chat Dtos
 */
export interface NewChatDto {
    usernames: string[],
    name?:string
}

/**
 * Message Dtos
 */
export interface DeleteMessageDto {
    id: number
}

export interface NewMessageDto {
    content: string,
    chatRoomId: number
}

export interface UpdateMessageDto {
    messageId: number,
    newContent: string
}