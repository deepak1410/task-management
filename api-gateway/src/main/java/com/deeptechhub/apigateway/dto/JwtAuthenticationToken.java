package com.deeptechhub.apigateway.dto;

import com.deeptechhub.common.dto.UserDto;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String token;
    private final UserDto user;

    public JwtAuthenticationToken(String token, UserDto user) {
        super(List.of(new SimpleGrantedAuthority(user.getRole().getAuthority())));
        this.token = token;
        this.user = user;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return user.getEmail();
    }
}