interface User {
    email: string,
    username: string,
    chatRooms: ChatRoomDto[],
    authDetails: AuthDetails
}

interface UserDto {
    id: number,
    username: string
}

interface ChatRoomDto {
    id: number,
    name: string
}

interface AuthDetails {
    id: number,
    isVerified: boolean,
    roles: string[]
}

interface ChatRoom {
    id: number,
    name: string,
    members: UserDto[],
    messages: Message[]
}

interface Message {
    id: number,
    content: string,
    author: string,
    timeSent: string
}