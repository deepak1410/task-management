package com.deeptechhub.taskservice.service;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TaskServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private IdentityServiceClient identityServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void testCreateTask() throws Exception {
        TaskRequest request = new TaskRequest("Task Title", "Task Description", LocalDateTime.now().plusDays(1));
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Task Title")));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testGetTaskById() throws Exception {
        Task task = new Task();
        task.setTitle("Sample");
        task.setDescription("Desc");
        task.setDueDate(LocalDateTime.now().plusDays(2));
        task.setCreatedByUserId(1L);
        Task saved = taskRepository.save(task);

        mockMvc.perform(get("/api/tasks/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Sample")));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testDeleteTask() throws Exception {
        Task task = new Task();
        task.setTitle("DeleteMe");
        task.setDescription("To delete");
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setCreatedByUserId(1L);
        Task saved = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    public void testUpdateTask() throws Exception {
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
                .andExpect(jsonPath("$.title", is("New")));
    }
}