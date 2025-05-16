package com.deeptechhub.identityservice.service;

import com.deeptechhub.identityservice.domain.RefreshToken;
import com.deeptechhub.identityservice.dto.AuthResponse;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.refreshTokenExpiryMs}")
    private long refreshTokenExpiryMs; // Used for generating new access token

    public void saveToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plus(refreshTokenExpiryMs, ChronoUnit.MILLIS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> getValidToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked() && rt.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        String username = jwtService.extractUsername(refreshTokenStr);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        Optional<RefreshToken> storedToken = getValidToken(refreshTokenStr);
        if(storedToken.isEmpty() || storedToken.get().isRevoked()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails.getUsername());
        return new AuthResponse(newAccessToken, refreshTokenStr);
    }
}
