package com.example.todolist.dto.request;

import com.example.todolist.entity.Status;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

  @Size(max = 30, message = "Task title must be at most 30 characters long")
  @NotNull(message = "Task title cannot be null")
  @NotBlank(message = "Task title cannot be blank")
  private String title;

  @Size(max = 255, message = "Task description must be at most 255 characters long")
  private String description;

  @NotNull(message = "Task status cannot be null")
  private Status status;

  @FutureOrPresent(message = "Task due date must be in the future")
  @NotNull(message = "Task due date cannot be null")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime dueDate;

  @NotNull(message = "Task category id cannot be null")
  private UUID categoryId;
}
