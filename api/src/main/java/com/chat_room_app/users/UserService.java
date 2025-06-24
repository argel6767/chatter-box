package com.chat_room_app.users;

import com.chat_room_app.chatroom.dtos.ChatRoomIdAndNameDto;
import com.chat_room_app.exceptions.NotFound404Exception;
import com.chat_room_app.friends.FriendStatus;
import com.chat_room_app.friends.Friendship;
import com.chat_room_app.friends.dtos.FriendIdAndNameDto;
import com.chat_room_app.users.dtos.UserProfileDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
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
        User searchedUser = getUserById(searchUserId);
        User requestingUser = getUserById(requesterUserId);

        Set<ChatRoomIdAndNameDto> commonChatRooms = searchedUser.getChatRooms().stream()
                .filter(chatRoom -> chatRoom.getMembers().containsAll(List.of(searchedUser, requestingUser)))
                .map(chatRoom -> new ChatRoomIdAndNameDto(chatRoom.getId(), chatRoom.getName()))
                .collect(Collectors.toSet());

        Set<FriendIdAndNameDto> mutualFriends = getMutualFriends(searchedUser, requestingUser);

        return new UserProfileDto(searchedUser.getUsername(), mutualFriends, commonChatRooms);
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByUsername(username);
    }
}
