package com.chat_room_app.auth.dtos;

public record ChangePasswordDto(String username, String oldPassword, String newPassword) {
}
