package com.chat_room_app.users.dtos;

import com.chat_room_app.chatroom.dtos.ChatRoomIdAndNameDto;
import com.chat_room_app.friends.FriendStatus;
import com.chat_room_app.friends.dtos.FriendIdAndNameDto;
import java.util.Set;


public record UserProfileDto(String username, Set<FriendIdAndNameDto> friends, Set<ChatRoomIdAndNameDto> commonChatRooms, FriendStatus status) {
}
