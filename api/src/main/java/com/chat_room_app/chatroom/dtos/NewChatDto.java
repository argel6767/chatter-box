package com.chat_room_app.chatroom.dtos;

import java.util.Set;

public record NewChatDto(Set<String> usernames, String name) {
}
