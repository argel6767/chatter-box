package com.chat_room_app.exceptions;

import com.chat_room_app.exceptions.custom_exceptions.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<FailedAPIRequestResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new FailedAPIRequestResponse("Incorrect credentials given", request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<FailedAPIRequestResponse> handleAccessDenied(AccessDeniedException ade, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new FailedAPIRequestResponse(ade.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<FailedAPIRequestResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new FailedAPIRequestResponse(ex.getMessage(), request.getRequestURI()));
    }


    @ExceptionHandler(NotFound404Exception.class)
    public ResponseEntity<FailedAPIRequestResponse> handleNotFound404(NotFound404Exception nfe, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new FailedAPIRequestResponse(nfe.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BadRequest400Exception.class)
    public ResponseEntity<FailedAPIRequestResponse> handleBadRequestException(BadRequest400Exception bre, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new FailedAPIRequestResponse(bre.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Conflict409Exception.class)
    public ResponseEntity<FailedAPIRequestResponse> handleConflict409Exception(Conflict409Exception ce, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new FailedAPIRequestResponse(ce.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UnAuthorized401Exception.class)
    public ResponseEntity<FailedAPIRequestResponse> handleUnauthorized401Exception(UnAuthorized401Exception ue, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new FailedAPIRequestResponse(ue.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<FailedAPIRequestResponse> handleMessagingException(MessagingException me, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new FailedAPIRequestResponse(me.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FailedAPIRequestResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException me, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new FailedAPIRequestResponse(me.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<FailedAPIRequestResponse> handleServiceUnavailableException(ServiceUnavailableException sue, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new FailedAPIRequestResponse(sue.getMessage(), request.getRequestURI()));
    }
}
