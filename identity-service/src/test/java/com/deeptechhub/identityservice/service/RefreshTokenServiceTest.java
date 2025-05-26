package com.deeptechhub.identityservice.service;

import com.deeptechhub.identityservice.domain.RefreshToken;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.AuthResponse;
import com.deeptechhub.identityservice.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiryMs", 600000L); // 10 minutes
    }

    @Test
    void saveToken_ShouldSaveValidRefreshToken() {
        User user = User.builder().id(1L).username("john").build();
        String token = "refresh-token";

        refreshTokenService.saveToken(user, token);

        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        RefreshToken savedToken = refreshTokenCaptor.getValue();

        assertEquals(token, savedToken.getToken());
        assertEquals(user, savedToken.getUser());
        assertFalse(savedToken.isRevoked());
        assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void getValidToken_ShouldReturnToken_IfNotRevokedAndNotExpired() {
        String token = "valid-token";
        RefreshToken storedToken = RefreshToken.builder()
                .token(token)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(storedToken));

        Optional<RefreshToken> result = refreshTokenService.getValidToken(token);

        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
    }

    @Test
    void getValidToken_ShouldReturnEmpty_IfRevokedOrExpired() {
        String token = "expired-token";
        RefreshToken revokedToken = RefreshToken.builder()
                .token(token)
                .revoked(true)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        RefreshToken expiredToken = RefreshToken.builder()
                .token(token)
                .revoked(false)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.of(revokedToken))
                .thenReturn(Optional.of(expiredToken));

        assertTrue(refreshTokenService.getValidToken(token).isEmpty());
        assertTrue(refreshTokenService.getValidToken(token).isEmpty());
    }

    @Test
    void refreshToken_ShouldReturnNewAccessToken_WhenTokenIsValid() {
        String refreshToken = "valid-refresh-token";
        String username = "john";
        String newAccessToken = "new-access-token";

        RefreshToken storedToken = RefreshToken.builder()
                .token(refreshToken)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, "pass", List.of()
        );

        when(jwtService.extractUsername(refreshToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(username)).thenReturn(newAccessToken);

        AuthResponse response = refreshTokenService.refreshToken(refreshToken);

        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsInvalidOrRevoked() {
        String token = "invalid-token";
        String username = "john";

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(
                new org.springframework.security.core.userdetails.User(username, "pass", List.of())
        );
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.refreshToken(token));
    }
}
