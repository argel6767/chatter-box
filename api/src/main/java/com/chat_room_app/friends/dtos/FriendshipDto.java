package com.chat_room_app.friends.dtos;

import com.chat_room_app.friends.FriendStatus;

public record FriendshipDto(FriendIdAndNameDto user, FriendIdAndNameDto friend, FriendStatus status) {
}
