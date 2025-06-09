package com.deeptechhub.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuration class for customizing the WebClient instance.
 * Sets up JSON serialization/deserialization, timeouts, and error handling.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates and configures a WebClient with custom JSON handling,
     * a 5-second response timeout, and error response processing.
     *
     * @param builder the WebClient builder to use
     * @param objectMapper the ObjectMapper for JSON serialization
     * @return a configured WebClient instance
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder, ObjectMapper objectMapper) {
        // Registers JavaTimeModule
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return builder
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(5))
                ))
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper)
                    );
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper)
                    );
                })
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "Service call failed: " + clientResponse.statusCode() + " - " + errorBody
                                )));
                    }
                    return Mono.just(clientResponse);
                }))
                .build();
    }
}