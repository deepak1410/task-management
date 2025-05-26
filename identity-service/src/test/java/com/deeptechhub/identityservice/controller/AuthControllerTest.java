package com.deeptechhub.identityservice.controller;

import com.deeptechhub.identityservice.config.JwtProperties;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.*;
import com.deeptechhub.identityservice.service.AuthService;
import com.deeptechhub.identityservice.service.RefreshTokenService;
import com.deeptechhub.identityservice.service.TokenBlacklistService;
import com.deeptechhub.identityservice.util.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private AuthController authController;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @Test
    void register_givenValidRequest_shouldReturnSuccessMessage() {
        RegisterRequest request = new RegisterRequest("testuser", "password", "test@example.com", "Test User");
        User user = new User();
        user.setUsername("testuser");

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(user);

        ResponseEntity<String> response = authController.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Registration successful"));
    }

    @Test
    void login_givenValidCredentials_shouldReturnAuthTokens() {
        AuthRequest request = new AuthRequest("user", "pass");
        AuthResponse authResponse = new AuthResponse("access", "refresh");

        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access", response.getBody().getAccessToken());
    }

    @Test
    void initiatePasswordReset_givenValidEmail_shouldTriggerResetProcess() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("email@example.com");

        doNothing().when(authService).initiatePasswordReset(any());

        ResponseEntity<Void> response = authController.initiatePasswordReset(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void resetPassword_givenValidTokenAndPassword_shouldResetPasswordSuccessfully() {
        ResetPasswordRequest request = new ResetPasswordRequest("token", "NewPass@123");

        doNothing().when(authService).resetPassword(any());

        ResponseEntity<Void> response = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verifyEmail_givenValidToken_shouldReturnSuccessMessage() {
        User user = new User();
        user.setUsername("user");

        when(authService.verifyEmail("validToken")).thenReturn(user);

        ResponseEntity<String> response = authController.verifyEmail("validToken");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Email successfully verified"));
    }

    @Test
    void refreshAccessToken_givenValidRefreshToken_shouldReturnNewAuthTokens() {
        AuthResponse authResponse = new AuthResponse("newAccess", "newRefresh");

        when(refreshTokenService.refreshToken("validRefreshToken")).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response =
                authController.refreshAccessToken(new AuthController.RefreshTokenRequest("validRefreshToken"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newAccess", response.getBody().getAccessToken());
    }

    @Test
    void logout_givenValidTokens_shouldBlacklistTokensAndReturnNoContent() {
        when(jwtProperties.getAccessTokenExpiryMs()).thenReturn(1000L);
        when(jwtProperties.getRefreshTokenExpiryMs()).thenReturn(2000L);

        try (MockedStatic<AuthUtils> mocked = Mockito.mockStatic(AuthUtils.class)) {
            mocked.when(() -> AuthUtils.extractToken(any())).thenReturn("access-token");

            ResponseEntity<Void> apiResponse = authController.logout(request, response, "refresh-token");

            assertEquals(HttpStatus.NO_CONTENT, apiResponse.getStatusCode());
            verify(tokenBlacklistService).blacklistToken(eq("access-token"), any());
            verify(tokenBlacklistService).blacklistToken(eq("refresh-token"), any());
        }
    }
}


