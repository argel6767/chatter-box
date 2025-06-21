package com.chat_room_app.message.dtos;

public record MessageDto(Long id, String content, String author, String timeSent) {
}
