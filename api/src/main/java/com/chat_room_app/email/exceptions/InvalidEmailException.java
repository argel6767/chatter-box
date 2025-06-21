package com.chat_room_app.email.exceptions;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
