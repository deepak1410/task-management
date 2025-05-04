package com.deeptechhub.taskservice.security;

import com.deeptechhub.common.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);

    boolean isTokenValid(String token, UserDto userDetails);
}
