package com.chat_room_app.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${access.token.header}")
    private String accessToken;

    @Bean
    RestClient restClient() {
        return RestClient
                .builder()
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.add("Access-Token", accessToken);
                    httpHeaders.add("Content-Type", "application/json");
                }).baseUrl(emailServiceUrl).build();
    }
}
