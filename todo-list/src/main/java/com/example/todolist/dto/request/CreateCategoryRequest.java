package com.example.todolist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class CreateCategoryRequest {
    @NotNull(message = "Category name cannot be null")
    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 50, message = "Category name must be at most 50 characters long")
    String name;

    @NotBlank(message = "Color cannot be blank")
    @Size(max = 10, message = "Color must be at most 10 characters long")
    String color;
}