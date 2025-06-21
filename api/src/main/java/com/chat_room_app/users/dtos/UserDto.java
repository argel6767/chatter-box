package com.chat_room_app.users.dtos;

import com.chat_room_app.auth.AuthDetails;
import com.chat_room_app.auth.dtos.AuthDto;
import com.chat_room_app.chatroom.dtos.ChatRoomIdAndNameDto;
import com.chat_room_app.users.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    String email;
    String username;
    Set<ChatRoomIdAndNameDto> chatRooms;
    AuthDto authDetails;

    public static UserDto getUserDto(User user) {
        Set<ChatRoomIdAndNameDto> chatRooms = user.getChatRooms().stream().map(chatRoom ->
                new ChatRoomIdAndNameDto(chatRoom.getId(), chatRoom.getName())).collect(Collectors.toSet());
        AuthDetails authDetails = user.getAuthDetails();
        List<String> roles = Arrays.stream(authDetails.getAuthorities().split(",")).toList();
        AuthDto authDto = new AuthDto(authDetails.getId(), authDetails.getIsVerified(), roles);
        return new UserDto(user.getEmail(), user.getUsername(), chatRooms, authDto);
    }
}
