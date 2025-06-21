package com.chat_room_app.message;

import com.chat_room_app.chatroom.ChatRoom;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String content;

    private String sender;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    private LocalDateTime created = LocalDateTime.now();
}
