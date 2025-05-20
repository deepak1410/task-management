package com.deeptechhub.identityservice.controller;

import com.deeptechhub.common.dto.Role;
import com.deeptechhub.identityservice.BaseIntegrationTest;
import com.deeptechhub.identityservice.domain.User;
import com.deeptechhub.identityservice.dto.UpdateProfileDTO;
import com.deeptechhub.identityservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends BaseIntegrationTest  {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        savedUser = User.builder()
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .password("password")
                .build();
        savedUser = userRepository.save(savedUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetCurrentUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateUserProfile() throws Exception {
        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setEmail("updated@example.com");
        dto.setName("Updated Name");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.name").value("Updated Name"));

        User updated = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testListAllUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserByUsername() throws Exception {
        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateUserRole() throws Exception {
        mockMvc.perform(patch("/api/users/" + savedUser.getId() + "/role")
                        .param("newRole", "ADMIN"))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testListAllUsersAsNonAdminShouldFail() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
}
