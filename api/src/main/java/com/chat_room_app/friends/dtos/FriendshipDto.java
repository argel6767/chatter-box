package com.chat_room_app.friends.dtos;

import com.chat_room_app.friends.FriendStatus;

public record FriendshipDto(Long id, FriendIdAndNameDto user, FriendIdAndNameDto friend, FriendStatus status) {
}
