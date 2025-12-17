package com.example.todolist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
    @Size(max = 50, message = "Category name must be at most 50 characters long")
        @NotNull
        @NotBlank(message = "Category name cannot be blank.")
        String name,
    @Size(max = 10, message = "Color must be at most 10 characters long")
        @NotNull
        @NotBlank(message = "Category color cannot be blank")
        String color) {}
