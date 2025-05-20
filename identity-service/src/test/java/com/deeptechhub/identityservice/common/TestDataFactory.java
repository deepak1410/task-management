package com.deeptechhub.identityservice.common;

import com.deeptechhub.common.dto.Role;
import com.deeptechhub.identityservice.domain.User;

public class TestDataFactory {
    public static User createValidUser() {
        return createValidUser("testuser", "test@example.com");
    }

    public static User createValidUser(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password("password")
                .name("Test User")
                .role(Role.USER)
                .enabled(true)
                .emailVerified(true)
                .build();
    }

    public static String validLoginRequestJson() {
        return """
            {
                "username": "testuser",
                "password": "password"
            }
            """;
    }

    public static String validRegistrationRequestJson() {
        return """
            {
                "username": "newuser",
                "email": "new@example.com",
                "password": "password",
                "name": "New User"
            }
            """;
    }

    public static String validTaskRequestJson() {
        return """
            {
                "title": "Test Task",
                "description": "Test Description"
            }
            """;
    }
}
