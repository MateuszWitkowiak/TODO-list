package com.example.todolist.dto.request;

import com.example.todolist.entity.Status;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTaskRequest(
        @Size(max = 30, message = "Task title must be at most 30 characters long")
        String title,
        @Size(max = 255, message = "Task description must be at most 255 characters long")
        String description,
        Status status,
        LocalDateTime dueDate,
        UUID categoryId
) {}
