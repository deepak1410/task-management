package com.deeptechhub.identityservice.service;

import com.deeptechhub.common.dto.Role;
import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.common.exception.ResourceNotFoundException;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.UpdateProfileDTO;
import com.deeptechhub.identityservice.dto.UserProfileDTO;
import com.deeptechhub.identityservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setName("John Doe");
        user.setRole(Role.USER);
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void findByUsername_whenUserExists_returnsUserDto() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserDto dto = userService.findByUsername("john");

        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void findByUsername_whenUserNotFound_throwsException() {
        when(userRepository.findByUsername("jane")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("jane"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with username jane not found");
    }

    @Test
    void findById_whenUserExists_returnsUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto dto = userService.findById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("john");
    }

    @Test
    void findById_whenUserNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id 1 not found");
    }

    @Test
    void getCurrentUserProfile_whenUserExists_returnsProfileDTO() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserProfileDTO dto = userService.getCurrentUserProfile("john");

        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
        assertThat(dto.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void getCurrentUserProfile_whenUserNotFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUserProfile("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with username unknown not found");
    }

    @Test
    void updateUserProfile_validUpdate_updatesAndReturnsProfileDTO() {
        UpdateProfileDTO updateDTO = UpdateProfileDTO.builder()
                .email("new@example.com")
                .name("New Name")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileDTO updated = userService.updateUserProfile("john", updateDTO);

        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    void updateUserProfile_userNotFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UpdateProfileDTO updateDTO = UpdateProfileDTO.builder().name("x").email("x@example.com").build();

        assertThatThrownBy(() -> userService.updateUserProfile("unknown", updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with username unknown not found");
    }

    @Test
    void listAllUsers_returnsListOfUserProfileDTO() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setEmail("admin@example.com");
        user2.setName("Admin");
        user2.setRole(Role.ADMIN);

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserProfileDTO> users = userService.listAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUsername()).isEqualTo("john");
        assertThat(users.get(1).getUsername()).isEqualTo("admin");
    }

    @Test
    void updateUserRole_validRole_updatesAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateUserRole(1L, Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRole_sameRole_noSaveCalled() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateUserRole(1L, Role.USER);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(99L, Role.ADMIN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User with id 99 not found");
    }
}
