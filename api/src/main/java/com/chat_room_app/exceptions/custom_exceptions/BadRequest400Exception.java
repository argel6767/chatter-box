package com.chat_room_app.exceptions.custom_exceptions;

public class BadRequest400Exception extends RuntimeException {
    public BadRequest400Exception(String message) {
        super(message);
    }
}
