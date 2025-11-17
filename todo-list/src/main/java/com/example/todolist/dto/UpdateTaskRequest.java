package com.example.todolist.dto;

import com.example.todolist.entity.Status;

import java.util.UUID;

public record UpdateTaskRequest(
        String title,
        String description,
        Status status,
        String dueDate,
        UUID categoryId
) {}
