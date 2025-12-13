package com.example.todolist.dto.request;

import com.example.todolist.entity.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    @Size(max = 30, message = "Task title must be at most 30 characters long")
    String title;

    @Size(max = 255, message = "Task description must be at most 255 characters long")
    String description;

    Status status;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime dueDate;

    UUID categoryId;
}
