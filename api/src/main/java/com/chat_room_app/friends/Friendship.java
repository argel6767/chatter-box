package com.chat_room_app.friends;

import com.chat_room_app.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "receiver_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FriendStatus status; // e.g., 'PENDING', 'ACCEPTED', etc.

    @Column(name = "friendship_date")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and setters
}
