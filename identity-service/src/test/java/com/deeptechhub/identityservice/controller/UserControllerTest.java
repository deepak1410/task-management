package com.deeptechhub.identityservice.controller;

import com.deeptechhub.common.dto.Role;
import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.identityservice.dto.UpdateProfileDTO;
import com.deeptechhub.identityservice.dto.UserProfileDTO;
import com.deeptechhub.identityservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Disables Spring security filter for isolated controller testing
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Mock
    private Authentication authentication;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCurrentUserProfile_ReturnsProfile() throws Exception {
        UserProfileDTO profile = new UserProfileDTO("deepak", "deepak@gmail.com", "Deepak", true, Role.USER);

        when(authentication.getName()).thenReturn("deepak");
        when(userService.getCurrentUserProfile("deepak")).thenReturn(profile);

        mockMvc.perform(get("/api/users/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("deepak"));
    }

    @Test
    void updateUserProfile_ReturnsUpdatedProfile() throws Exception {
        UpdateProfileDTO updateDto = new UpdateProfileDTO("Updated Name", "updated@gmail.com");
        UserProfileDTO updatedProfile = new UserProfileDTO("deepak", "updated@gmail.com", "Updated Name", true, Role.USER);

        when(authentication.getName()).thenReturn("deepak");
        when(userService.updateUserProfile(eq("deepak"), any(UpdateProfileDTO.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/users/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listAllUsers_AsAdmin_ReturnsUserList() throws Exception {
        List<UserProfileDTO> userList = List.of(
                new UserProfileDTO("user1", "user1@gmail.com", "User One", true, Role.USER),
                new UserProfileDTO("user2", "user2@gmail.com", "User Two", false, Role.ADMIN)
        );

        when(userService.listAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    void getUserById_ReturnsUserDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("deepak")
                .email("deepak@gmail.com")
                .name("Deepak")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.findById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("deepak"));
    }

    @Test
    void getUserByUsername_ReturnsUserDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .name("Admin User")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.findByUsername("admin")).thenReturn(userDto);

        mockMvc.perform(get("/api/users/username/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateUserRole_AsAdmin_ReturnsNoContent() throws Exception {
        Long userId = 10L;
        Role newRole = Role.ADMIN;

        mockMvc.perform(patch("/api/users/{userId}/role", userId)
                        .param("newRole", newRole.name()))
                .andExpect(status().isNoContent());

        verify(userService).updateUserRole(userId, newRole);
    }
}

