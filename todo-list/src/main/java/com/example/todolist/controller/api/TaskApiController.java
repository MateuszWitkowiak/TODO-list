package com.example.todolist.controller.api;

import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.response.CreateTaskResponse;
import com.example.todolist.dto.response.GetTaskResponse;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.dto.mapper.TaskMapper;
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
    private final TaskMapper taskMapper;

    public TaskApiController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public ResponseEntity<List<GetTaskResponse>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        List<GetTaskResponse> response = taskMapper.mapToGetTaskResponse(tasks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetTaskResponse> getTaskById(@PathVariable("id") UUID id) {
        Task task = taskService.findTaskById(id);
        GetTaskResponse response = taskMapper.mapToGetTaskResponse(task);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GetTaskResponse>> searchTasks(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        List<Task> tasks = taskService.searchTasksByTitle(keyword, page, size);
        List<GetTaskResponse> response = taskMapper.mapToGetTaskResponse(tasks);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreateTaskResponse> createTask(@RequestBody @Valid CreateTaskRequest dto) {
        Task created = taskService.createTask(dto);
        CreateTaskResponse response = taskMapper.mapToCreateTaskResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetTaskResponse> updateTask(
            @PathVariable("id") UUID id,
            @RequestBody @Valid UpdateTaskRequest dto
    ) {
        Task updated = taskService.updateTask(id, dto);
        GetTaskResponse response = taskMapper.mapToGetTaskResponse(updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") UUID id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
