package com.deeptechhub.identityservice.service;

import com.deeptechhub.common.dto.Role;
import com.deeptechhub.common.exception.ResourceNotFoundException;
import com.deeptechhub.identityservice.domain.EmailToken;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.*;
import com.deeptechhub.identityservice.repository.EmailTokenRepository;
import com.deeptechhub.identityservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String USERNAME = "johndoe";
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "password";
    private static final String HASHED_PASSWORD = "hashedPassword";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String FULL_NAME = "John Doe";
    private static final String TOKEN_STRING = "token123";

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private EmailService emailService;
    @Mock private UserRepository userRepository;
    @Mock private EmailTokenRepository emailTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    // ---------- Register User Tests ----------

    @Test
    @DisplayName("Register user - should save user with encoded password and send verification email")
    void registerUser_whenValidRequest_thenUserIsSaved() {
        RegisterRequest request = new RegisterRequest(USERNAME, PASSWORD, EMAIL, FULL_NAME);
        User expectedUser = createUser(1L, USERNAME, EMAIL, FULL_NAME);

        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(emailTokenRepository.save(any(EmailToken.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.registerUser(request);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(HASHED_PASSWORD, result.getPassword());
        assertFalse(result.isEmailVerified());
        assertFalse(result.isEnabled());

        verify(userRepository).save(any(User.class));
        verify(emailTokenRepository).save(any(EmailToken.class));
        verify(emailService).sendVerificationEmail(any(User.class), any(EmailToken.class));
    }

    // ---------- Login Tests ----------

    @Test
    @DisplayName("Login - should return tokens when credentials are valid")
    void login_whenValidCredentials_thenReturnAccessAndRefreshTokens() {
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
        User user = createUser(1L, USERNAME, EMAIL, FULL_NAME);
        user.setEnabled(true);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                USERNAME, HASHED_PASSWORD, List.of(new SimpleGrantedAuthority(Role.USER.name())));

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null));
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(USERNAME)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(USERNAME)).thenReturn(REFRESH_TOKEN);

        AuthResponse response = authService.login(authRequest);

        assertEquals(ACCESS_TOKEN, response.getAccessToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        verify(refreshTokenService).saveToken(user, REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Login - should throw when user not found")
    void login_whenUserNotFound_thenThrowException() {
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null));
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // Change expected exception to NoSuchElementException
        assertThrows(NoSuchElementException.class, () -> authService.login(authRequest));
    }

    @Test
    @DisplayName("Login - should throw when account is disabled")
    void login_whenAccountDisabled_thenThrowException() {
        AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);

        // Create a disabled UserDetails
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                USERNAME,
                HASHED_PASSWORD,
                false,  // enabled = false
                true, true, true,
                List.of(new SimpleGrantedAuthority(Role.USER.name())));

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("Account is disabled"));

        assertThrows(DisabledException.class, () -> authService.login(authRequest));

        // Verify userRepository is never called
        verify(userRepository, never()).findByUsername(any());
    }

    // ---------- Password Reset Tests ----------

    @Test
    @DisplayName("Initiate password reset - should send email when user exists")
    void initiatePasswordReset_whenEmailExists_thenSendResetEmail() {
        User user = createUser(1L, USERNAME, EMAIL, FULL_NAME);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(emailTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.initiatePasswordReset(new ForgotPasswordRequest(EMAIL));

        verify(emailService).sendPasswordResetEmail(eq(user), any(EmailToken.class));
        verify(emailTokenRepository).save(any(EmailToken.class));
    }

    @Test
    @DisplayName("Initiate password reset - should throw when email not found")
    void initiatePasswordReset_whenEmailNotFound_thenThrowException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.initiatePasswordReset(new ForgotPasswordRequest(EMAIL)));

        verify(emailService, never()).sendPasswordResetEmail(any(), any());
        verify(emailTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password - should update password when token is valid")
    void resetPassword_whenValidToken_thenUpdatePasswordAndMarkTokenUsed() {
        ResetPasswordRequest request = new ResetPasswordRequest(TOKEN_STRING, "newPass");
        User user = createUser(1L, USERNAME, EMAIL, FULL_NAME);
        EmailToken token = createEmailToken(TOKEN_STRING, user);

        when(emailTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(eq(TOKEN_STRING), any()))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");

        authService.resetPassword(request);

        assertEquals("encodedPass", user.getPassword());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(emailTokenRepository).save(token);
    }

    @Test
    @DisplayName("Reset password - should throw when token is invalid")
    void resetPassword_whenInvalidToken_thenThrowException() {
        ResetPasswordRequest request = new ResetPasswordRequest(TOKEN_STRING, "newPass");

        when(emailTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(eq(TOKEN_STRING), any()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).save(any());
        verify(emailTokenRepository, never()).save(any());
    }

    // ---------- Email Verification Tests ----------

    @Test
    @DisplayName("Verify email - should mark email verified when token is valid")
    void verifyEmail_whenValidToken_thenMarkEmailVerifiedAndTokenUsed() {
        User user = createUser(1L, USERNAME, EMAIL, FULL_NAME);
        user.setEmailVerified(false);
        EmailToken token = createEmailToken(TOKEN_STRING, user);

        when(emailTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(eq(TOKEN_STRING), any()))
                .thenReturn(Optional.of(token));
        when(userRepository.save(user)).thenReturn(user);

        User result = authService.verifyEmail(TOKEN_STRING);

        assertTrue(result.isEmailVerified());
        assertTrue(token.isUsed());
        verify(emailTokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Verify email - should throw when token is invalid")
    void verifyEmail_whenInvalidToken_thenThrowException() {
        when(emailTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(eq(TOKEN_STRING), any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmail(TOKEN_STRING));

        verify(userRepository, never()).save(any());
        verify(emailTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Verify email - should throw when email already verified")
    void verifyEmail_whenEmailAlreadyVerified_thenThrowException() {
        User user = createUser(1L, USERNAME, EMAIL, FULL_NAME);
        user.setEmailVerified(true);
        EmailToken token = createEmailToken(TOKEN_STRING, user);

        when(emailTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(eq(TOKEN_STRING), any()))
                .thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmail(TOKEN_STRING));

        verify(userRepository, never()).save(any());
        verify(emailTokenRepository, never()).save(any());
    }

    // ---------- Helper Methods ----------

    private User createUser(Long id, String username, String email, String name) {
        return User.builder()
                .id(id)
                .username(username)
                .password(HASHED_PASSWORD)
                .email(email)
                .name(name)
                .role(Role.USER)
                .enabled(false)
                .locked(false)
                .emailVerified(false)
                .build();
    }

    private EmailToken createEmailToken(String token, User user) {
        return EmailToken.builder()
                .token(token)
                .user(user)
                .used(false)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();
    }
}
