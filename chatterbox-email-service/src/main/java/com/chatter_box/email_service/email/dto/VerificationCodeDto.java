package com.chatter_box.email_service.email.dto;

public record VerificationCodeDto(String email, String username, String code) {
}
