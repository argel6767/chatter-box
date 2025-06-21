package com.chat_room_app.auth.dtos;

import java.util.List;

public record AuthDto(Long id, Boolean isVerified, List<String> roles) {
}
