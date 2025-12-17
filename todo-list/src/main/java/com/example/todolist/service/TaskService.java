package com.example.todolist.service;

import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.User;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.service.filter.TaskFilter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private static final Logger log = LoggerFactory.getLogger(TaskService.class);

  public TaskService(
      TaskRepository taskRepository,
      CategoryRepository categoryRepository,
      UserRepository userRepository,
      UserService userService) {
    this.taskRepository = taskRepository;
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
    this.userService = userService;
  }

  @Transactional
  public void deleteTaskById(UUID taskId) {
    taskRepository.deleteById(taskId);
  }

  @Transactional(readOnly = true)
  public List<Task> getAllTasks() {
    User user = userService.getCurrentUser();
    return taskRepository.findAllByUserId(user.getId());
  }

  @Transactional(readOnly = true)
  public Page<Task> getAllTasks(TaskFilter filter) {
    UUID userId = userService.getCurrentUser().getId();
    int page = Math.max(filter.getPage(), 0);
    int size = Math.max(filter.getSize(), 1);

    String sortProperty =
        (filter.getSort() == null || filter.getSort().isBlank()) ? "title" : filter.getSort();
    Sort.Direction dir =
        "desc".equalsIgnoreCase(filter.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sortObj = Sort.by(dir, sortProperty);
    PageRequest pageRequest = PageRequest.of(page, size, sortObj);

    LocalDateTime dueAfter =
        filter.getDueAfter() != null ? filter.getDueAfter().atStartOfDay() : null;
    LocalDateTime dueBefore =
        filter.getDueBefore() != null ? filter.getDueBefore().atTime(LocalTime.MAX) : null;
    Status status = null;
    try {
      if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
        status = Status.valueOf(filter.getStatus());
      }
    } catch (IllegalArgumentException ignored) {
      log.warn("Invalid status filter '{}', skipping status filter", filter.getStatus());
    }

    return taskRepository.searchTasksByFilter(
        userId, null, status, filter.getCategoryId(), dueAfter, dueBefore, pageRequest);
  }

  @Transactional(readOnly = true)
  public Page<Task> searchTasksByTitle(TaskFilter filter) {
    UUID userId = userService.getCurrentUser().getId();
    int page = Math.max(filter.getPage(), 0);
    int size = Math.max(filter.getSize(), 1);

    String keyword =
        (filter.getTitle() != null && !filter.getTitle().isBlank()) ? filter.getTitle() : "";

    String sortProperty =
        (filter.getSort() == null || filter.getSort().isBlank()) ? "title" : filter.getSort();
    Sort.Direction dir =
        "desc".equalsIgnoreCase(filter.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sortObj = Sort.by(dir, sortProperty);

    PageRequest pageRequest = PageRequest.of(page, size, sortObj);

    LocalDateTime dueAfter =
        filter.getDueAfter() != null ? filter.getDueAfter().atStartOfDay() : null;
    LocalDateTime dueBefore =
        filter.getDueBefore() != null ? filter.getDueBefore().atTime(LocalTime.MAX) : null;
    Status status = null;
    try {
      if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
        status = Status.valueOf(filter.getStatus());
      }
    } catch (IllegalArgumentException ignored) {
      log.warn("Invalid status filter '{}', skipping status filter", filter.getStatus());
    }

    return taskRepository.searchTasksByFilter(
        userId, keyword, status, filter.getCategoryId(), dueAfter, dueBefore, pageRequest);
  }

  @Transactional(readOnly = true)
  public Task findTaskById(UUID taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Id", taskId));
  }

  @Transactional
  public Task updateTask(UUID taskId, UpdateTaskRequest dto) {
    Task task =
        taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("id", taskId));

    task.setTitle(dto.getTitle());
    if (dto.getDescription() != null) {
      task.setDescription(dto.getDescription());
    }
    if (dto.getStatus() != null) {
      task.setStatus(dto.getStatus());
    }
    if (dto.getDueDate() != null) {
      task.setDueDate(dto.getDueDate());
    }
    if (dto.getCategoryId() != null) {
      Category category =
          categoryRepository
              .findById(dto.getCategoryId())
              .orElseThrow(() -> new CategoryNotFoundException("id", dto.getCategoryId()));
      task.setCategory(category);
    }

    return taskRepository.save(task);
  }

  @Transactional
  public Task createTask(CreateTaskRequest dto) {
    Task task = new Task();

    task.setTitle(dto.getTitle());
    task.setDescription(dto.getDescription());
    task.setStatus(dto.getStatus());
    task.setDueDate(dto.getDueDate());

    Category category =
        categoryRepository
            .findById(dto.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException("id", dto.getCategoryId()));
    task.setCategory(category);

    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    task.setUser(user);

    return taskRepository.save(task);
  }

  @Transactional(readOnly = true)
  public Map<String, Long> getStats() {
    UUID userId = userService.getCurrentUser().getId();
    Map<String, Long> stats = new HashMap<>();

    long total = taskRepository.countByUserId(userId);
    long todo = taskRepository.countByUserIdAndStatus(userId, Status.TODO);
    long inProgress = taskRepository.countByUserIdAndStatus(userId, Status.IN_PROGRESS);
    long done = taskRepository.countByUserIdAndStatus(userId, Status.DONE);

    stats.put("totalTasks", total);
    stats.put("todoTasks", todo);
    stats.put("inProgressTasks", inProgress);
    stats.put("doneTasks", done);

    return stats;
  }

  @Transactional(readOnly = true)
  public Page<Task> getUpcomingTasks() {
    return taskRepository.findByUserIdAndDueDateIsNotNullOrderByDueDateAsc(
        userService.getCurrentUser().getId(), PageRequest.of(0, 5));
  }

  @Transactional
  public void writeTasksCsvToResponse(HttpServletResponse response) {
    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=\"tasks.csv\"");

    User user = userService.getCurrentUser();
    List<Task> tasks = taskRepository.findAllByUserId(user.getId());

    try (OutputStreamWriter osw =
            new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
        CSVWriter writer =
            new CSVWriter(
                osw,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
      writer.writeNext(new String[] {"title", "description", "status", "dueDate", "categoryName"});
      for (Task t : tasks) {
        writer.writeNext(
            new String[] {
              t.getTitle() != null ? t.getTitle() : "",
              t.getDescription() != null ? t.getDescription() : "",
              t.getStatus() != null ? t.getStatus().name() : "",
              t.getDueDate() != null ? t.getDueDate().toString() : "",
              t.getCategory() != null ? t.getCategory().getName() : ""
            });
      }
      writer.flush();
    } catch (Exception ex) {
      throw new RuntimeException("CSV export failed", ex);
    }
  }

  @Transactional
  public void importTasksFromCsv(MultipartFile file) {
    User user = userService.getCurrentUser();

    try (InputStreamReader isr =
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
         CSVReader reader = new CSVReaderBuilder(isr)
                 .withCSVParser(new com.opencsv.CSVParserBuilder().withSeparator(';').build())
                 .build()) {
      String[] row;
      boolean skipHeader = true;
      while ((row = reader.readNext()) != null) {
        if (skipHeader) {
          skipHeader = false;
          continue;
        }
        Task task = new Task();
        task.setTitle(row.length > 0 ? row[0] : null);
        task.setDescription(row.length > 1 ? row[1] : null);
        task.setStatus(
            row.length > 2 && row[2] != null && !row[2].isBlank()
                ? Status.valueOf(row[2])
                : Status.TODO);
        task.setDueDate(
            row.length > 3 && row[3] != null && !row[3].isBlank()
                ? LocalDateTime.parse(row[3])
                : null);

        if (row.length > 4 && row[4] != null && !row[4].isBlank()) {
          Category category =
              categoryRepository.findByNameAndUserId(row[4], user.getId()).orElse(null);
          task.setCategory(category);
        }
        task.setUser(user);
        taskRepository.save(task);
      }
    } catch (CsvValidationException e) {
      throw new RuntimeException("CSV validation failed: ", e);
    } catch (Exception e) {
      throw new RuntimeException("CSV import failed: ", e);
    }
  }
}
