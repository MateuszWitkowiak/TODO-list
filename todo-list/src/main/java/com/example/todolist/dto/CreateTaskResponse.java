package com.example.todolist.dto;


import com.example.todolist.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskResponse {
    private UUID id;

    private String title;

    private String description;

    private Status status;

    private LocalDateTime dueDate;

    private UUID categoryId;
}
