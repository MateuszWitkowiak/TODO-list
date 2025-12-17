package com.example.todolist.controller.api;

import com.example.todolist.dto.mapper.TaskMapper;
import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.dto.response.CreateTaskResponse;
import com.example.todolist.dto.response.GetTaskResponse;
import com.example.todolist.entity.Task;
import com.example.todolist.service.TaskService;
import com.example.todolist.service.filter.TaskFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
  public ResponseEntity<Page<GetTaskResponse>> searchTasks(
      @ModelAttribute TaskFilter filter, @RequestParam("keyword") String keyword) {
    filter.setTitle(keyword);

    Page<Task> tasks = taskService.searchTasksByTitle(filter);
    Page<GetTaskResponse> responsePage = taskMapper.mapToGetTaskResponse(tasks);

    return ResponseEntity.ok(responsePage);
  }

  @PostMapping
  public ResponseEntity<CreateTaskResponse> createTask(@RequestBody @Valid CreateTaskRequest dto) {
    Task created = taskService.createTask(dto);
    CreateTaskResponse response = taskMapper.mapToCreateTaskResponse(created);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<GetTaskResponse> updateTask(
      @PathVariable("id") UUID id, @RequestBody @Valid UpdateTaskRequest dto) {
    Task updated = taskService.updateTask(id, dto);
    GetTaskResponse response = taskMapper.mapToGetTaskResponse(updated);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable("id") UUID id) {
    taskService.deleteTaskById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/export")
  public void exportTasksCsv(HttpServletResponse response) {
    taskService.exportTasksToCSV(response);
  }

  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> importTasksCsv(@RequestParam("file") MultipartFile file) {
    try {
      taskService.importTasksFromCsv(file);
      return ResponseEntity.ok("Import zakończony sukcesem!");
    } catch (Exception ex) {
      return ResponseEntity.badRequest().body("Błąd importu CSV: " + ex.getMessage());
    }
  }
}
