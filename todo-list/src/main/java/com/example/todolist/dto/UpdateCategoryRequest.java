package com.example.todolist.dto;

import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Size(max = 50, message = "Category name must be at most 50 characters long")
        String name,
        @Size(max = 10, message = "Color must be at most 10 characters long")
        String color
) {}