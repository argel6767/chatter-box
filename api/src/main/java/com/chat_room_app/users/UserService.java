package com.chat_room_app.users;

import com.chat_room_app.exceptions.NotFound404Exception;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFound404Exception("user not found: " + username));
    }

    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }
}
