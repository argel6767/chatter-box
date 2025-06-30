package com.chat_room_app.users;

import com.chat_room_app.jwt.JwtUtils;
import com.chat_room_app.users.dtos.QueriedUserDto;
import com.chat_room_app.users.dtos.UserDto;
import com.chat_room_app.users.dtos.UserProfileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> get() {
        String username = JwtUtils.getCurrentUserUsername();
        User user = userService.getUserByUsername(username);
        UserDto userDto = UserDto.getUserDto(user);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> delete() {
        String username = JwtUtils.getCurrentUserUsername();
        userService.deleteUserByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{searchUserId}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable Long searchUserId) {
        Long requesterId = JwtUtils.getCurrentUserId();
        UserProfileDto dto = userService.getUserProfile(searchUserId, requesterId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/query")
    public ResponseEntity<List<QueriedUserDto>> queryUsers(@RequestParam String query) {
        List<QueriedUserDto> dto = userService.queryUsers(query);
        return ResponseEntity.ok(dto);
    }
    
}
