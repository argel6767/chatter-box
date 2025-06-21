package com.chat_room_app.exceptions;

public class NotFound404Exception extends RuntimeException {
    public NotFound404Exception(String message) {
        super(message);
    }
}
