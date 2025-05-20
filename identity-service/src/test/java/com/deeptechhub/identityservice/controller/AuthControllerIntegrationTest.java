package com.deeptechhub.identityservice.controller;

import com.deeptechhub.identityservice.BaseIntegrationTest;
import com.deeptechhub.identityservice.domain.EmailToken;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.*;
import com.deeptechhub.identityservice.repository.EmailTokenRepository;
import com.deeptechhub.identityservice.repository.RefreshTokenRepository;
import com.deeptechhub.identityservice.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailTokenRepository emailTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        // Clean up repositories before each test
        refreshTokenRepository.deleteAll();
        emailTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerUser_ShouldReturnSuccessMessage() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "testuser",
                "test@example.com",
                "Test@1234",
                "Test"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful. Please verify your email before logging in"));
    }

    @Test
    void registerUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "", // invalid username
                "invalid-email", // invalid email
                "short", // invalid password
                "" // invalid name
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        // Register user
        RegisterRequest registerRequest = new RegisterRequest(
                "loginuser",
                "Login@1234",
                "login@example.com",
                "Login"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Fetch user and token from DB
        User user = userRepository.findByUsername("loginuser").orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Login
        AuthRequest authRequest = new AuthRequest("loginuser", "Login@1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest("nonexistent", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void initiatePasswordReset_WithValidEmail_ShouldReturnOk() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "forgotuser", "Forgot@123", "forgot@example.com", "Forgot");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Fetch user and token from DB
        User user = userRepository.findByUsername("forgotuser").orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Login
        AuthRequest authRequest = new AuthRequest("forgotuser", "Forgot@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest("forgot@example.com");

        mockMvc.perform(post("/api/auth/forgot-pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_WithValidToken_ShouldReturnOk() throws Exception {
        // Simulate user and token
        User user = new User();
        user.setUsername("resetuser");
        user.setEmail("reset@example.com");
        user.setPassword("encodedOldPassword");
        user.setName("Reset");
        user.setEmailVerified(true);
        user = userRepository.save(user);

        EmailToken token = new EmailToken();
        token.setToken("reset-token");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        emailTokenRepository.save(token);

        ResetPasswordRequest resetRequest = new ResetPasswordRequest("reset-token", "NewPass@123");

        mockMvc.perform(post("/api/auth/reset-pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void verifyEmail_WithValidToken_ShouldReturnSuccess() throws Exception {
        User user = new User();
        user.setUsername("verifyuser");
        user.setEmail("verify@example.com");
        user.setPassword("encodedPassword");
        user.setName("Verify");
        user.setEmailVerified(false);
        user = userRepository.save(user);

        EmailToken token = new EmailToken();
        token.setToken("verify-token");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        emailTokenRepository.save(token);

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "verify-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email successfully verified"));
    }

    @Test
    void verifyEmail_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAuthTokens() throws Exception {
        // Register and login to get refreshToken
        RegisterRequest registerRequest = new RegisterRequest(
                "refreshtokenuser", "Refresh@123", "refresh@example.com",  "Refresh");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        User user = userRepository.findByUsername("refreshtokenuser").orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        AuthRequest loginRequest = new AuthRequest("refreshtokenuser", "Refresh@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String refreshToken = jsonNode.get("refreshToken").asText();

        // Refresh token
        Map<String, String> refreshTokenRequest = Map.of("refreshToken", refreshToken);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void logout_ShouldClearCookiesAndInvalidateTokens() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "logoutuser",  "Logout@123", "logout@example.com", "Logout");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        User user = userRepository.findByUsername("logoutuser").orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        AuthRequest loginRequest = new AuthRequest("logoutuser", "Logout@123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();
        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", refreshToken))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_ShouldInvalidateTokens() throws Exception {
        // Register user
        RegisterRequest registerRequest = new RegisterRequest(
                "logoutuser",
                "Logout@1234",
                "logout@example.com",
                "Logout"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Fetch user and set email as verified
        User user = userRepository.findByUsername("logoutuser").orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Login
        AuthRequest authRequest = new AuthRequest("logoutuser", "Logout@1234");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + authResponse.getAccessToken())
                        .cookie(new Cookie("refreshToken", authResponse.getRefreshToken())))
                .andExpect(status().isNoContent());
    }

}
