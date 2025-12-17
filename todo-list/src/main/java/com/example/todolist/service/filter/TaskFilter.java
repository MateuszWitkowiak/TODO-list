package com.example.todolist.service.filter;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskFilter {
  private String title;
  private String sort;
  private String direction;
  private String status;
  private UUID categoryId;
  private LocalDate dueAfter;
  private LocalDate dueBefore;
  private int page = 0;
  private int size = 10;
}
