package com.chat_room_app.exceptions.custom_exceptions;

public class UnAuthorized401Exception extends RuntimeException {
    public UnAuthorized401Exception(String message) {
        super(message);
    }
}
