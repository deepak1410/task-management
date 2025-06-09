package com.deeptechhub.apigateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("Public endpoint works!");
    }
}
