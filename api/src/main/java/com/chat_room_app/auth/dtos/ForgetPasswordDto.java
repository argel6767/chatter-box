package com.chat_room_app.auth.dtos;

public record ForgetPasswordDto(String username, String newPassword, String verificationCode) {
}
