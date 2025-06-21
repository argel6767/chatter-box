package com.chat_room_app.auth;

import com.chat_room_app.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AuthDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long id;

    private String verificationCode;

    private LocalDateTime codeExpiryTime;

    private Boolean isVerified = false;

    private String authorities;

    private LocalDateTime createdAt = LocalDateTime.now();
}
