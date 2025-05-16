package com.deeptechhub.taskservice.service;

import com.deeptechhub.common.exception.ResourceNotFoundException;
import com.deeptechhub.taskservice.domain.Task;
import com.deeptechhub.taskservice.dto.TaskRequest;
import com.deeptechhub.taskservice.dto.TaskResponse;
import com.deeptechhub.taskservice.repository.TaskRepository;
import com.deeptechhub.taskservice.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;

    public TaskResponse createTask(TaskRequest taskRequest) {
        Long createdByUserId = SecurityUtils.getCurrentUser().getId();

        // Get task from request object
        Task task = new Task();
        task.setTitle(taskRequest.title());
        task.setDescription(taskRequest.description());
        task.setDueDate(taskRequest.dueDate());
        task.setCreatedByUserId(createdByUserId);

        // save task
        log.info("Attempting to save task {}", task);
        Task createdTask = taskRepository.save(task);
        log.info("Successfully saved task {}", task);

        return TaskResponse.fromTask(createdTask);
    }

    public List<TaskResponse> getUserTasks(String username) {
        Long createdByUserId = SecurityUtils.getCurrentUser().getId();

        return taskRepository.findByCreatedByUserId(createdByUserId).stream()
                .map(TaskResponse::fromTask)
                .toList();
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(TaskResponse::fromTask)
                .toList();
    }

    public TaskResponse getTask(Long id) {
        return taskRepository.findById(id)
                .map(TaskResponse::fromTask)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public void deleteTask(Long id) {
        if(!taskRepository.existsById(id)) {
            log.warn("No tasks have been found with id {}", id);
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    public TaskResponse updateTask(Long id, TaskRequest taskRequest) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Update the existingTask
        if(StringUtils.isNotBlank(taskRequest.title())) {
            existingTask.setTitle(taskRequest.title());
        }

        if(StringUtils.isNotBlank(taskRequest.description())) {
            existingTask.setDescription(taskRequest.description());
        }

        if(taskRequest.dueDate() != null) {
            existingTask.setDueDate(taskRequest.dueDate());
        }

        return TaskResponse.fromTask(taskRepository.save(existingTask));
    }

}
