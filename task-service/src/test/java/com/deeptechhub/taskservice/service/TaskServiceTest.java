package com.deeptechhub.taskservice.service;

import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.common.exception.ResourceNotFoundException;
import com.deeptechhub.taskservice.domain.Task;
import com.deeptechhub.taskservice.dto.TaskRequest;
import com.deeptechhub.taskservice.dto.TaskResponse;
import com.deeptechhub.taskservice.repository.TaskRepository;
import com.deeptechhub.taskservice.security.SecurityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTask() {
        TaskRequest request = new TaskRequest("Title", "Description", LocalDateTime.now().plusDays(1));
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setCreatedByUserId(1L);

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle(task.getTitle());
        savedTask.setDescription(task.getDescription());
        savedTask.setDueDate(task.getDueDate());
        savedTask.setCreatedByUserId(task.getCreatedByUserId());

        UserDto mockUserDto = mock(UserDto.class);
        mockUserDto.setId(1L);

        when(securityHelper.getCurrentUser()).thenReturn(mockUserDto);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("Title", response.title());
        assertEquals("Description", response.description());
    }

    @Test
    public void testGetTaskFound() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task1");
        task.setDescription("Description");
        task.setDueDate(LocalDateTime.now().plusDays(1));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTask(1L);

        assertEquals(1L, response.id());
        assertEquals("Task1", response.title());
    }

    @Test
    public void testGetTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTask(1L));
    }

    @Test
    public void testDeleteTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        taskService.deleteTask(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    public void testDeleteTaskNotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(1L));
    }

    @Test
    public void testUpdateTask() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setDescription("Old Desc");

        TaskRequest update = new TaskRequest("New", "New Desc", LocalDateTime.now().plusDays(2));
        Task updated = new Task();
        updated.setId(1L);
        updated.setTitle("New");
        updated.setDescription("New Desc");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenReturn(updated);

        TaskResponse response = taskService.updateTask(1L, update);

        assertEquals("New", response.title());
        assertEquals("New Desc", response.description());
    }

    @Test
    public void testUpdateTaskNotFound() {
        TaskRequest request = new TaskRequest("New", "New Desc", LocalDateTime.now().plusDays(1));
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, request));
    }
}