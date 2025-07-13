package com.chat_room_app.friends;

import com.chat_room_app.friends.dtos.FriendshipDto;
import com.chat_room_app.jwt.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request/{friendId}")
    public ResponseEntity<FriendshipDto> requestFriendship(@PathVariable("friendId") long friendId) {
        Long userId = JwtUtils.getCurrentUserId();
        FriendshipDto dto = friendshipService.requestFriendship(userId, friendId);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PutMapping("/accept/{friendshipId}")
    public ResponseEntity<FriendshipDto> acceptFriendship(@PathVariable("friendshipId") long friendshipId) {
        FriendshipDto dto = friendshipService.acceptFriendship(friendshipId);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @DeleteMapping("/remove/{friendshipId}")
    public ResponseEntity<Void> removeFriendship(@PathVariable("friendshipId") long friendshipId) {
         friendshipService.removeFriendship(friendshipId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/block/{friendId}")
    public ResponseEntity<FriendshipDto> blockFriendship(@PathVariable("friendId") long friendId) {
        Long userId = JwtUtils.getCurrentUserId();
        FriendshipDto dto = friendshipService.blockUser(userId, friendId);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/un-block/{friendId}")
    public ResponseEntity<String> unBlockFriendship(@PathVariable("friendId") long friendId) {
        Long userId = JwtUtils.getCurrentUserId();
        friendshipService.unBlockUser(userId, friendId);
        return new ResponseEntity<>("User unblocked", HttpStatus.NO_CONTENT);
    }

    @GetMapping()
    public ResponseEntity<Set<FriendshipDto>> getFriends() {
        Long userId = JwtUtils.getCurrentUserId();
        Set<FriendshipDto> friends = friendshipService.getAllFriends(userId);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @GetMapping("/requests")
    public ResponseEntity<Set<FriendshipDto>> getFriendRequests() {
        Long userId = JwtUtils.getCurrentUserId();
        Set<FriendshipDto> friendRequests = friendshipService.getAllFriendRequests(userId);
        return new ResponseEntity<>(friendRequests, HttpStatus.OK);
    }

    @GetMapping("/blocked")
    public ResponseEntity<Set<FriendshipDto>> getBlocked() {
        Long userId = JwtUtils.getCurrentUserId();
        Set<FriendshipDto> blockedUsers = friendshipService.getAllBlockedUsers(userId);
        return new ResponseEntity<>(blockedUsers, HttpStatus.OK);
    }

}
