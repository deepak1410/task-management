package com.deeptechhub.common.security;

import com.deeptechhub.common.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);

    boolean isTokenValid(String token, String username);

    String generateAccessToken(String username);

    String generateRefreshToken(String username);
}
