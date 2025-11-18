package com.example.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotNull(message = "Category name cannot be null")
        @NotBlank(message = "Category name cannot be blank")
        @Size(max = 50, message = "Category name must be at most 50 characters long")
        String name,

        @NotBlank(message = "Color cannot be blank")
        @Size(max = 10, message = "Color must be at most 10 characters long")
        String color
) {}