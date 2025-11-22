package com.example.todolist.dto;

import com.example.todolist.entity.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        Status status,
        LocalDateTime dueDate,
        UUID categoryId,
        String categoryName
) {}
