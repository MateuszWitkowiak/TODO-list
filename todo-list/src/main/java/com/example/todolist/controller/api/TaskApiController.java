package com.example.todolist.controller.api;

import com.example.todolist.dto.CreateTaskRequest;
import com.example.todolist.dto.UpdateTaskRequest;
import com.example.todolist.entity.Task;
import com.example.todolist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskApiController {

    private final TaskService taskService;

    public TaskApiController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.findTaskById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(taskService.searchTasksByTitle(keyword, page, size));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody @Valid CreateTaskRequest dto) {
        Task created = taskService.createTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateTaskRequest dto
    ) {
        Task updated = taskService.updateTask(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
