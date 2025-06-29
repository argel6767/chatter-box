import {AuthDetails, ChatRoomDto, ChatRoomUserDto} from "@/lib/models/responses";

export interface User {
    id: number,
    email: string,
    username: string,
    chatRooms: ChatRoomDto[],
    authDetails: AuthDetails
}

export interface ChatRoom {
    id: number,
    name: string,
    creator: string,
    members: ChatRoomUserDto[],
    messages: Message[]
}

export interface Message {
    id: number,
    content: string,
    author: string,
    timeSent: string
}
