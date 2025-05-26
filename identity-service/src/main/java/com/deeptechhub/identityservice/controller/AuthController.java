package com.deeptechhub.identityservice.controller;

import com.deeptechhub.identityservice.config.JwtProperties;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.*;
import com.deeptechhub.identityservice.service.AuthService;
import com.deeptechhub.identityservice.service.RefreshTokenService;
import com.deeptechhub.identityservice.service.TokenBlacklistService;
import com.deeptechhub.identityservice.util.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name="Authentication", description = "User registration and authentication APIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    public record RefreshTokenRequest(@NotBlank String refreshToken) { }

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtProperties jwtProperties;

    @Operation(summary = "Register a new user")
    @PostMapping(path = "/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Attempting to register a user with username {}", request.getUsername());
        User user = authService.registerUser(request);
        log.debug("Successfully registered user {}", user.getUsername());
        return ResponseEntity.ok("Registration successful. Please verify your email before logging in");
    }

    @Operation(summary = "Authenticate user")
    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        log.debug("Attempting to login user ");
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/forgot-pwd")
    public ResponseEntity<Void> initiatePasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        log.debug("Attempting to initiate password reset for email {}", request.getEmail());
        authService.initiatePasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-pwd")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.debug("Attempting to reset password");
        authService.resetPassword(request);
        log.debug("Password has been successfully reset");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        log.debug("Attempting to verify email for token {}", token);
        User verifiedUser = authService.verifyEmail(token);
        return ResponseEntity.ok("Email successfully verified");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshAccessToken(@RequestBody @Valid RefreshTokenRequest request) {
        log.debug("Attempting to refresh token");
        AuthResponse authResponse = refreshTokenService.refreshToken(request.refreshToken());
        log.debug("Successfully refreshed token");
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response,
                                       @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        // Invalidate accessToken
        String accessToken = AuthUtils.extractToken(request);
        tokenBlacklistService.blacklistToken(accessToken,
                Duration.ofMillis(jwtProperties.getAccessTokenExpiryMs()));

        // Invalidate refresh token if exists
        if (refreshToken != null) {
            tokenBlacklistService.blacklistToken(refreshToken,
                    Duration.ofMillis(jwtProperties.getRefreshTokenExpiryMs()));
        }

        // Clear cookies
        clearAuthCookies(response);

        // Clear security context
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api/auth/refresh-token");
        refreshCookie.setMaxAge(0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

}
