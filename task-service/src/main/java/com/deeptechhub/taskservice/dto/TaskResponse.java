package com.deeptechhub.taskservice.dto;

import com.deeptechhub.taskservice.domain.Task;

import java.time.LocalDateTime;


public record TaskResponse(
    Long id,
    String title,
    String description,
    String createdBy, // username
    LocalDateTime dueDate,
    boolean completed
){
    public static TaskResponse fromTask(Task task) {
        //TODO: Update the createdByUserId here
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(),
                "" + task.getCreatedByUserId(), task.getDueDate(), task.isCompleted());
    }
}
