package com.deeptechhub.taskservice.controller;

import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.taskservice.BaseIntegrationTest;
import com.deeptechhub.taskservice.client.IdentityServiceClient;
import com.deeptechhub.taskservice.domain.Task;
import com.deeptechhub.taskservice.dto.TaskRequest;
import com.deeptechhub.taskservice.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class TaskControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private IdentityServiceClient identityServiceClient;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        UserDto mockUser = new UserDto();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        when(identityServiceClient.getUserById(anyLong())).thenReturn(mockUser);
        when(identityServiceClient.getUserByUsername(anyString())).thenReturn(mockUser);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void createTask_shouldSucceed() throws Exception {
        TaskRequest request = new TaskRequest("Integration Task", "Test Desc", LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Task"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getUserTasks_shouldReturnTasks() throws Exception {
        Task task = new Task();
        task.setTitle("Title");
        task.setDescription("Desc");
        task.setDueDate(LocalDateTime.now().plusDays(2));
        task.setCreatedByUserId(1L);
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTasks_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/tasks/all"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getTaskById_shouldSucceed() throws Exception {
        Task task = new Task();
        task.setTitle("Sample");
        task.setDescription("Sample Desc");
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setCreatedByUserId(1L);
        Task saved = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void deleteTaskById_shouldSucceed() throws Exception {
        Task task = new Task();
        task.setTitle("Delete Me");
        task.setDescription("To be deleted");
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setCreatedByUserId(1L);
        Task saved = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void updateTask_shouldSucceed() throws Exception {
        Task task = new Task();
        task.setTitle("Old");
        task.setDescription("Old Desc");
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setCreatedByUserId(1L);
        Task saved = taskRepository.save(task);

        TaskRequest update = new TaskRequest("New", "New Desc", LocalDateTime.now().plusDays(2));

        mockMvc.perform(put("/api/tasks/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"));
    }
}
