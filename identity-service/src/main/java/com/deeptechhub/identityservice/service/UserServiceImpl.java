package com.deeptechhub.identityservice.service;


import com.deeptechhub.common.dto.Role;
import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.UpdateProfileDTO;
import com.deeptechhub.identityservice.dto.UserProfileDTO;
import com.deeptechhub.common.exception.ResourceNotFoundException;
import com.deeptechhub.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("User with username " + username + " not found"));
        log.info("Found user {}", username);

        return getUserDtoFromUser(user);
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User with id " + id + " not found"));
        return getUserDtoFromUser(user);
    }

    public UserProfileDTO getCurrentUserProfile(String username) {
        log.debug("Attempting to get user profile for {}", username);
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("User with username " + username + " not found"));

        return UserProfileDTO.fromUser(user);
    }

    public UserProfileDTO updateUserProfile(String username, UpdateProfileDTO updateProfileDTO) {
        User existingUser = userRepository.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("User with username " + username + " not found"));

        if(updateProfileDTO.getEmail() != null) {
            existingUser.setEmail(updateProfileDTO.getEmail());
        }

        if(updateProfileDTO.getName() != null) {
            existingUser.setName(updateProfileDTO.getName());
        }

        User user = userRepository.save(existingUser);
        return UserProfileDTO.fromUser(user);
    }

    public List<UserProfileDTO> listAllUsers() {
        return userRepository.findAll().stream()
                .map(UserProfileDTO::fromUser)
                .toList();
    }

    @Override
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User with id " + userId + " not found"));

        if(user.getRole() == newRole) {
            return; // No change needed;
        }
        user.setRole(newRole);
        userRepository.save(user);
    }

    private UserDto getUserDtoFromUser(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
