package com.chat_room_app.email;

import com.chat_room_app.auth.dtos.VerifyUserDto;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;

@Service
@Log
public class ChatterBoxEmailService {
    private final RestClient restClient;


    public ChatterBoxEmailService(RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendVerifyEmail(VerifyUserDto verifyUserDto) {
        restClient.post()
                .uri("/api/v1/emails/verify")
                .body(verifyUserDto)
                .retrieve();
    }

    public void sendRestPasswordEmail(VerifyUserDto verifyUserDto) {
        restClient.post()
                .uri("/api/v1/emails/reset-password")
                .body(verifyUserDto)
                .retrieve();
    }
}
