package com.chat_room_app.friends;

import com.chat_room_app.exceptions.custom_exceptions.BadRequest400Exception;
import com.chat_room_app.exceptions.custom_exceptions.Conflict409Exception;
import com.chat_room_app.exceptions.custom_exceptions.NotFound404Exception;
import com.chat_room_app.friends.dtos.FriendIdAndNameDto;
import com.chat_room_app.friends.dtos.FriendshipDto;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Log
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a Friendship entity with a PENDING status with the user the "requester"
     * @param userId
     * @param potentialFriendId
     * @return
     */
    public FriendshipDto requestFriendship(Long userId, Long potentialFriendId) {
        if (userId.equals(potentialFriendId)) {
            log.warning("User attempted to friend their self: " + userId);
            throw new Conflict409Exception("Cannot send friend request to yourself");
        }

        if (friendshipExists(userId, potentialFriendId)) {
            log.warning("Friendship is already made");
            throw new Conflict409Exception("Friendship already exists");
        }

        User user = findUserById(userId);
        User potentialFriend = findUserById(potentialFriendId);

        Friendship friendship = new Friendship();
        friendship.setRequester(user);
        friendship.setReceiver(potentialFriend);
        friendship.setStatus(FriendStatus.PENDING);

        Friendship saved = friendshipRepository.save(friendship);
        log.info("New friend request created between " + user.getUsername() + " and " + potentialFriend.getUsername());
        return createFriendshipDto(saved.getId(), user, potentialFriend, saved.getStatus());
    }

    /**
     * Accepts a friend request, changing the status to ACCEPTED
     * @param friendshipId
     * @return
     */
    public FriendshipDto acceptFriendship(Long friendshipId) {
        Friendship friendship = findFriendshipById(friendshipId);
        friendship.setStatus(FriendStatus.ACCEPTED);
        User receiver = friendship.getReceiver();
        User requester = friendship.getRequester();
        return createFriendshipDto(friendshipId, receiver, requester, FriendStatus.ACCEPTED);
    }

    /**
     * Removes friend, will be also used for any decline requests
     * @param friendshipId
     */
    public void removeFriendship(Long friendshipId) {
        Friendship friendship = findFriendshipById(friendshipId);
        log.info("Removing friendship: " + friendshipId);
        friendshipRepository.delete(friendship);
    }

    /**
     * Blocks a user changing the status to BLOCKED
     * @param userId
     * @param blockId
     * @return
     */
    public FriendshipDto blockUser(Long userId, Long blockId) {
        if (userId.equals(blockId)) {
            throw new Conflict409Exception("Cannot block yourself");
        }

        User user = findUserById(userId);
        User blockedUser = findUserById(blockId);

        // Remove existing friendship if it exists (either direction)
        removeExistingFriendship(userId, blockId);

        Friendship blockFriendship = new Friendship();
        blockFriendship.setRequester(user);
        blockFriendship.setReceiver(blockedUser);
        blockFriendship.setStatus(FriendStatus.BLOCKED);

        friendshipRepository.save(blockFriendship);
        return createFriendshipDto(blockFriendship.getId(), user, blockedUser, FriendStatus.BLOCKED);
    }

    public void unBlockUser(Long userId, Long blockId) {
        if (userId.equals(blockId)) {
            throw new Conflict409Exception("Cannot block yourself");
        }

        Friendship blockRelationship = friendshipRepository.findByRequesterIdAndReceiverId(userId, blockId)
                .orElseThrow(() -> new BadRequest400Exception("User is not blocked"));
        friendshipRepository.delete(blockRelationship);
    }

    /**
     * Fetches all friends of user
     * @param userId
     * @return
     */
    public Set<FriendshipDto> getAllFriends(Long userId) {
        User currentUser = findUserById(userId);
        log.info("Fetching all friends of " + currentUser.getUsername());
        Set<FriendshipDto> friends = new HashSet<>();

        // Friends where current user is the requester
        friends.addAll(
                friendshipRepository.findAllByRequesterAndStatus(currentUser, FriendStatus.ACCEPTED)
                        .stream()
                        .map(friendship -> createFriendshipDto(friendship.getId(), currentUser, friendship.getReceiver(), friendship.getStatus()))
                        .collect(Collectors.toSet())
        );

        // Friends where current user is the receiver
        friends.addAll(
                friendshipRepository.findAllByReceiverAndStatus(currentUser, FriendStatus.ACCEPTED)
                        .stream()
                        .map(friendship -> createFriendshipDto(friendship.getId(), currentUser, friendship.getRequester(), friendship.getStatus()))
                        .collect(Collectors.toSet())
        );

        return friends;
    }

    /**
     * Fetchs all blocked users
     * @param userId
     * @return
     */
    public Set<FriendshipDto> getAllBlockedUsers(Long userId) {
        User currentUser = findUserById(userId);
        return friendshipRepository.findAllByRequesterAndStatus(currentUser, FriendStatus.BLOCKED)
                .stream()
                .map(friendship -> createFriendshipDto(friendship.getId(), currentUser, friendship.getReceiver(), friendship.getStatus()))
                .collect(Collectors.toSet());
    }

    /**
     * Fetches all active friend requests
     * @param userId
     * @return
     */
    public Set<FriendshipDto> getAllFriendRequests(Long userId) {
        User currentUser = findUserById(userId);
        return friendshipRepository.findAllByReceiverAndStatus(currentUser, FriendStatus.PENDING)
                .stream()
                .map(friendship -> createFriendshipDto(friendship.getId(), currentUser, friendship.getRequester(), friendship.getStatus()))
                .collect(Collectors.toSet());
    }

    // Helper methods
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFound404Exception("User not found with id: " + userId));
    }

    private Friendship findFriendshipById(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NotFound404Exception("Friendship not found with id: " + friendshipId));
    }

    private boolean friendshipExists(Long userId1, Long userId2) {
        return friendshipRepository.findByRequesterIdAndReceiverId(userId1, userId2).isPresent() ||
                friendshipRepository.findByRequesterIdAndReceiverId(userId2, userId1).isPresent();
    }

    private void removeExistingFriendship(Long userId1, Long userId2) {
        friendshipRepository.findByRequesterIdAndReceiverId(userId1, userId2)
                .ifPresent(friendshipRepository::delete);
        friendshipRepository.findByRequesterIdAndReceiverId(userId2, userId1)
                .ifPresent(friendshipRepository::delete);
    }

    private FriendshipDto createFriendshipDto(Long friendshipId, User user, User friend, FriendStatus status) {
        FriendIdAndNameDto userDto = new FriendIdAndNameDto(user.getId(), user.getUsername());
        FriendIdAndNameDto friendDto = new FriendIdAndNameDto(friend.getId(), friend.getUsername());
        return new FriendshipDto(friendshipId, userDto, friendDto, status);
    }
}
