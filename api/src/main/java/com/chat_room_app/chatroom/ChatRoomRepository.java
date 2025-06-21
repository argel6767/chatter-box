package com.chat_room_app.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
