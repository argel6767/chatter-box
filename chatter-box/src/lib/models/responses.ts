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

export interface FriendIdAndNameDto {
    id: number,
    username: string
}

export interface FriendshipDto {
    id: number,
    user: FriendIdAndNameDto,
    friend: FriendIdAndNameDto,
    status: "ACCEPTED" | "PENDING" | "BLOCKED"
}

export interface QueriedUserDto {
    id: number,
    username: string
}

export interface UserProfileDto {
    username: string,
    friends: FriendIdAndNameDto[] //mutual friends
    commonChatRooms: ChatRoomDto[]
}

export interface DirectMessageDto {
    id: number;
    name: string,
    user: QueriedUserDto;
    otherMember: QueriedUserDto;
}
