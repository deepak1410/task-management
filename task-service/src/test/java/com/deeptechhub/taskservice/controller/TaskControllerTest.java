package com.deeptechhub.taskservice.controller;

import com.deeptechhub.taskservice.dto.TaskRequest;
import com.deeptechhub.taskservice.dto.TaskResponse;
import com.deeptechhub.taskservice.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private Principal principal;

    @InjectMocks
    private TaskController taskController;

    @Test
    void createTask_shouldReturnCreatedResponse() {
        TaskRequest request = new TaskRequest("Title", "Desc", LocalDateTime.now().plusDays(1));
        TaskResponse response = new TaskResponse(1L, "Title", "Desc", "user1", request.dueDate(), false);

        when(taskService.createTask(request)).thenReturn(response);
        when(principal.getName()).thenReturn("user1");

        ResponseEntity<TaskResponse> result = taskController.createTask(request, principal);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("/tasks" + response.id(), result.getHeaders().getLocation().toString());
        assertEquals(response, result.getBody());
    }

    @Test
    void getUserTasks_shouldReturnTaskList() {
        TaskResponse task = new TaskResponse(1L, "Title", "Desc", "user1", LocalDateTime.now(), false);
        when(taskService.getUserTasks("user1")).thenReturn(List.of(task));
        when(principal.getName()).thenReturn("user1");

        List<TaskResponse> result = taskController.getUserTasks(principal);

        assertEquals(1, result.size());
        assertEquals("Title", result.get(0).title());
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        TaskResponse task = new TaskResponse(1L, "Title", "Desc", "admin", LocalDateTime.now(), false);
        when(taskService.getAllTasks()).thenReturn(List.of(task));

        List<TaskResponse> result = taskController.getAllTasks(principal);

        assertEquals(1, result.size());
    }

    @Test
    void getTaskById_shouldReturnTask() {
        TaskResponse task = new TaskResponse(1L, "Title", "Desc", "user1", LocalDateTime.now(), false);
        when(taskService.getTask(1L)).thenReturn(task);

        ResponseEntity<TaskResponse> result = taskController.getTaskById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(task, result.getBody());
    }

    @Test
    void deleteTaskById_shouldReturnNoContent() {
        ResponseEntity<Void> result = taskController.deleteTaskById(1L);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(taskService).deleteTask(1L);
    }

    @Test
    void updateTask_shouldReturnUpdatedTask() {
        TaskRequest request = new TaskRequest("Updated", "Updated Desc", LocalDateTime.now().plusDays(2));
        TaskResponse updated = new TaskResponse(1L, "Updated", "Updated Desc", "user1", request.dueDate(), false);

        when(taskService.updateTask(1L, request)).thenReturn(updated);

        ResponseEntity<TaskResponse> result = taskController.updateTask(request, 1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(updated, result.getBody());
    }
}
