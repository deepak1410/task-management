package com.deeptechhub.taskservice.security;

import com.deeptechhub.common.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsMapper {
    public UserDetails toUserDetails(UserDto userDto) {
        if(userDto == null || userDto.getUsername() == null || userDto.getRole() == null) {
            throw new IllegalArgumentException("User must have non-null username and role");
        }

        return User
                .withUsername(userDto.getUsername())
                .password("N/A") // Dummy password; not used for actual authentication here
                .authorities(new SimpleGrantedAuthority(userDto.getRole().getAuthority()))
                .build();
    }
}
