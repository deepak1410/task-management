package com.deeptechhub.taskservice.security;

import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.taskservice.client.IdentityServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHelper {
    private final IdentityServiceClient identityServiceClient;

    public UserDto getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth != null) {
            if(auth.getDetails() instanceof UserDto userDto) {
                return userDto;
            }

            // Fallback: if it's test or default Spring auth
            if(auth.getPrincipal() instanceof  User user) {
                return identityServiceClient.getUserByUsername(user.getUsername());
            }
        }

        throw new IllegalArgumentException("User details not found in SecurityContext");
    }
}
