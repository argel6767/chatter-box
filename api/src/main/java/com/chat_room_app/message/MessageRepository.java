package com.chat_room_app.message;

import com.chat_room_app.chatroom.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByChatRoom(ChatRoom chatRoom);
}
