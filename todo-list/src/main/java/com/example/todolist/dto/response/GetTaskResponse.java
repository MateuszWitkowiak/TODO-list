package com.example.todolist.dto.response;

import com.example.todolist.entity.Status;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskResponse {
  private UUID id;

  private String title;

  private String description;

  private Status status;

  private LocalDateTime dueDate;

  private UUID categoryId;

  private UUID userId;
}
