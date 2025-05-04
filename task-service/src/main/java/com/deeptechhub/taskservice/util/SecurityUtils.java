package com.deeptechhub.taskservice.util;

import com.deeptechhub.common.dto.UserDto;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static UserDto getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.getDetails() instanceof UserDto userDto) {
            return userDto;
        }

        throw new IllegalArgumentException("User details not found in SecurityContext");
    }
}
