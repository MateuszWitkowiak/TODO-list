package com.example.todolist.dto;

public record CreateCategoryRequest(
        String name,
        String color
) {}