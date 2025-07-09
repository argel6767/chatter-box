package com.chat_room_app.exceptions.custom_exceptions;

public class Conflict409Exception extends RuntimeException {
    public Conflict409Exception(String message) {
        super(message);
    }
}
