package com.chat_room_app.chatroom.dtos;

import com.chat_room_app.message.dtos.MessageDto;
import com.chat_room_app.users.dtos.ChatRoomUserDto;


import java.util.List;

public record ChatRoomDto(Long id, String name, String creator, List<ChatRoomUserDto> members, List<MessageDto> messages) {
}
