package com.chat_room_app.auth;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<AuthDetails, Long> {
}
