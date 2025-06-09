package com.deeptechhub.apigateway.client;

import com.deeptechhub.common.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class IdentityServiceClient {
    private final WebClient webClient;

    public IdentityServiceClient(WebClient.Builder webClientBuilder,
                                 @Value("${identity-service.url}") String url) {
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    public Mono<UserDto> getUserByUsername(String username, String authToken) {
        return webClient.get()
                .uri("/api/users/username/{username}", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .bodyToMono(UserDto.class);
    }
}
