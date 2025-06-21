package com.chat_room_app.exceptions;

public class UnAuthorized401Exception extends RuntimeException {
    public UnAuthorized401Exception(String message) {
        super(message);
    }
}
