package com.deeptechhub.identityservice.controller;

import com.deeptechhub.common.dto.Role;
import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.identityservice.dto.UpdateProfileDTO;
import com.deeptechhub.identityservice.dto.UserProfileDTO;
import com.deeptechhub.identityservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getCurrentUserProfile(username));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateUserProfile(Authentication authentication,
                                                            @Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateUserProfile(username, updateProfileDTO));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> listAllUsers() {
        log.debug("Attempting to get all the users");
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable("id") Long id) {
        log.debug("Attempting to get user by id {}", id);
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("username") String username) {
        log.debug("Attempting to get user by username {}", username);
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "newRole") Role newRole
    ) {
        log.debug("Attempting to update role for userId {} with role {}", userId, newRole);
        userService.updateUserRole(userId, newRole);
        return ResponseEntity.noContent().build();
    }

}
