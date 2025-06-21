package com.chat_room_app.auth.dtos;

public record VerifyUserDto(String email, String username, String code) {
}
