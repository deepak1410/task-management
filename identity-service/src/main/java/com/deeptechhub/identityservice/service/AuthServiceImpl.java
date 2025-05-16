package com.deeptechhub.identityservice.service;

import com.deeptechhub.common.dto.Role;
import com.deeptechhub.identityservice.domain.EmailToken;
import com.deeptechhub.identityservice.domain.TokenType;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.*;
import com.deeptechhub.common.exception.ResourceNotFoundException;
import com.deeptechhub.identityservice.repository.EmailTokenRepository;
import com.deeptechhub.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .name(request.getName())
                .role(Role.USER) // Default role
                .enabled(false)
                .locked(false)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Generate Email verification token and send email
        EmailToken emailToken = saveEmailToken(user, TokenType.VERIFY_EMAIL, LocalDateTime.now().plusHours(24));
        emailService.sendVerificationEmail(user, emailToken);
        return savedUser;
    }

    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        // Generating Access token and refresh token
        String accessToken = jwtService.generateAccessToken(userDetails.getUsername());
        String refreshToken = jwtService.generateRefreshToken(userDetails.getUsername());

        // Save refreshToken
        refreshTokenService.saveToken(user, refreshToken);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + request.getEmail()));

        log.debug("User has been found with email {}", request.getEmail());

        EmailToken emailToken = saveEmailToken(user, TokenType.RESET_PASSWORD, LocalDateTime.now().plusHours(1));
        emailService.sendPasswordResetEmail(user, emailToken);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        EmailToken token = emailTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(
                        request.getToken(), LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired token"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user); // Update user

        token.setUsed(true);
        emailTokenRepository.save(token); // Update EmailToken
    }

    @Override
    public User verifyEmail(String tokenStr) {
        EmailToken token = emailTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(tokenStr, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired token"));

        if (token.getUser().isEmailVerified()) {
            throw new ResourceNotFoundException("Email already verified");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        User verifiedUser = userRepository.save(user);

        token.setUsed(true);
        emailTokenRepository.save(token);
        return verifiedUser;
    }

    private EmailToken saveEmailToken(User user, TokenType tokenType, LocalDateTime expiry) {
        EmailToken token = EmailToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .type(tokenType)
                .used(false)
                .expiryDate(expiry)
                .build();

        emailTokenRepository.save(token);
        log.debug("Email token has been saved");
        return token;
    }

}
