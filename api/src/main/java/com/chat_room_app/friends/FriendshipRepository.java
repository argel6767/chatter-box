package com.chat_room_app.friends;

import com.chat_room_app.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByRequesterIdAndReceiverId(Long userId, Long friendId);
    List<Friendship> findAllByRequesterAndStatus(User requester, FriendStatus status);
    List<Friendship> findAllByReceiverAndStatus(User receiver, FriendStatus status);
}
