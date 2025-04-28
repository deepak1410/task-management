package com.deeptechhub.taskservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;


public record TaskRequest(
    @NotBlank(message = "Title is mandatory") String title,
    String description,
    @FutureOrPresent(message = "Due date must be in future") LocalDateTime dueDate
    ) {}
