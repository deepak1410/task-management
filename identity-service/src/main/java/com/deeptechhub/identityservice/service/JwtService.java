package com.deeptechhub.identityservice.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    String generateAccessToken(String username);

    String generateRefreshToken(String username);
}
