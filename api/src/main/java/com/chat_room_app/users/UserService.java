package com.chat_room_app.users;

import com.chat_room_app.chatroom.dtos.ChatRoomIdAndNameDto;
import com.chat_room_app.exceptions.custom_exceptions.BadRequest400Exception;
import com.chat_room_app.exceptions.custom_exceptions.NotFound404Exception;
import com.chat_room_app.exceptions.custom_exceptions.UnAuthorized401Exception;
import com.chat_room_app.friends.FriendStatus;
import com.chat_room_app.friends.Friendship;
import com.chat_room_app.friends.dtos.FriendIdAndNameDto;
import com.chat_room_app.users.dtos.QueriedUserDto;
import com.chat_room_app.users.dtos.UserProfileDto;
import lombok.extern.java.Log;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFound404Exception("user not found: " + username));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFound404Exception("user not found: " + id));
    }

    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }

    /**
     * Gets basic users profile info
     * Also shows the requesting user their mutual chat rooms and friends
     * @param searchUserId
     * @param requesterUserId
     * @return
     */
    public UserProfileDto getUserProfile(Long searchUserId, Long requesterUserId) {
        log.info("Fetching user " + searchUserId + ", requested by " + requesterUserId);
        User searchedUser = getUserById(searchUserId);
        User requestingUser = getUserById(requesterUserId);

        FriendStatus status = getRelationshipStatus(requestingUser, searchedUser);

        Set<ChatRoomIdAndNameDto> commonChatRooms = searchedUser.getChatRooms().stream()
                .filter(chatRoom -> chatRoom.getMembers().containsAll(List.of(searchedUser, requestingUser)))
                .map(chatRoom -> new ChatRoomIdAndNameDto(chatRoom.getId(), chatRoom.getName()))
                .collect(Collectors.toSet());

        Set<FriendIdAndNameDto> mutualFriends = getMutualFriends(searchedUser, requestingUser);

        log.info("fetched profile for " + searchedUser.getUsername());
        return new UserProfileDto(searchedUser.getUsername(), mutualFriends, commonChatRooms, status);
    }

    /**
     * Gets the relationship status between two users
     * @param requester The user making the request
     * @param target The user being searched
     * @return FriendStatus representing the relationship
     */
    private FriendStatus getRelationshipStatus(User requester, User target) {
        // Check if requester sent a friendship request to target
        Optional<Friendship> sentFriendship = requester.getSentFriendships().stream()
                .filter(friendship -> friendship.getReceiver().getId().equals(target.getId()))
                .findFirst();

        if (sentFriendship.isPresent()) {
            return sentFriendship.get().getStatus();
        }

        // Check if requester received a friendship request from target
        Optional<Friendship> receivedFriendship = requester.getReceivedFriendships().stream()
                .filter(friendship -> friendship.getRequester().getId().equals(target.getId()))
                .findFirst();

        if (receivedFriendship.isPresent()) {
            if (receivedFriendship.get().getStatus().equals(FriendStatus.BLOCKED)) {
                throw new UnAuthorized401Exception("You are blocked by the user  " + target.getUsername());
            }
            return receivedFriendship.get().getStatus();
        }

        // No relationship exists
        return FriendStatus.NONE; // Assuming you have a NONE status in your enum
    }

    private Set<FriendIdAndNameDto> getMutualFriends(User user1, User user2) {
        Set<User> user1Friends = getAllFriends(user1);
        Set<User> user2Friends = getAllFriends(user2);

        return user1Friends.stream()
                .filter(user2Friends::contains)
                .map(friend -> new FriendIdAndNameDto(friend.getId(), friend.getUsername()))
                .collect(Collectors.toSet());
    }

    private Set<User> getAllFriends(User user) {
        Set<User> friends = new HashSet<>();

        // Friends where user sent the request
        user.getSentFriendships().stream()
                .filter(friendship -> friendship.getStatus().equals(FriendStatus.ACCEPTED))
                .map(Friendship::getReceiver)
                .forEach(friends::add);

        // Friends where user received the request
        user.getReceivedFriendships().stream()
                .filter(friendship -> friendship.getStatus().equals(FriendStatus.ACCEPTED))
                .map(Friendship::getRequester)
                .forEach(friends::add);

        return friends;
    }

    /**
     * Finds all users whose usernames match the query given
     * @param query
     * @return
     */
    public List<QueriedUserDto> queryUsers(String query) {
        if (query == null || query.isBlank()) {
            throw new BadRequest400Exception("Query cannot be empty");
        }
        query = query.trim().toLowerCase();

        if (query.length() < 3) { //too short of a query
            return new ArrayList<>();
        }
        log.info("fetch users with query: "+ query);
        List<User> queriedUsers = userRepository.findAllByUsernameLike(query);
        return queriedUsers.stream()
                .map(user -> new QueriedUserDto(user.getId(), user.getUsername())).toList();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByUsername(username);
    }
}
