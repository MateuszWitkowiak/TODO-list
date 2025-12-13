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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Locale;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    public TaskService(TaskRepository taskRepository,
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
    public List<Task> getAllTasks(String sort,
                                  String direction,
                                  String status,
                                  UUID categoryId,
                                  LocalDate dueAfter,
                                  LocalDate dueBefore) {

        UUID userId = userService.getCurrentUser().getId();

        List<Task> tasks = taskRepository.findAllByUserId(userId);

        Stream<Task> stream = tasks.stream();

        if (status != null && !status.isBlank()) {
            try {
                Status enumStatus = Status.valueOf(status);
                stream = stream.filter(t -> t.getStatus() == enumStatus);
            } catch (IllegalArgumentException ignored) {
                log.warn("Invalid status filter '{}', skipping status filter", status);
            }
        }

        if (categoryId != null) {
            stream = stream.filter(t -> t.getCategory() != null && categoryId.equals(t.getCategory().getId()));
        }

        if (dueAfter != null) {
            LocalDateTime from = dueAfter.atStartOfDay();
            stream = stream.filter(t -> t.getDueDate() != null && !t.getDueDate().isBefore(from));
        }

        if (dueBefore != null) {
            LocalDateTime to = dueBefore.atTime(LocalTime.MAX);
            stream = stream.filter(t -> t.getDueDate() != null && !t.getDueDate().isAfter(to));
        }

        List<Task> filtered = stream.collect(Collectors.toList());

        Collator polishCollator = Collator.getInstance(new Locale("pl", "PL"));
        polishCollator.setStrength(Collator.PRIMARY);

        Comparator<Task> comparator;
        switch (sort) {
            case "description" -> comparator = Comparator.comparing(
                    Task::getDescription,
                    Comparator.nullsLast(polishCollator)
            );
            case "dueDate" -> comparator = Comparator.comparing(
                    Task::getDueDate,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "status" -> comparator = Comparator.comparing(
                    Task::getStatus,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "category.name" -> comparator = Comparator.comparing(
                    (Task t) -> t.getCategory() != null ? t.getCategory().getName() : null,
                    Comparator.nullsLast(polishCollator)
            );
            default -> comparator = Comparator.comparing(
                    Task::getTitle,
                    Comparator.nullsLast(polishCollator)
            );
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        filtered.sort(comparator);
        return filtered;
    }

    @Transactional(readOnly = true)
    public Task findTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Id", taskId));
    }

    @Transactional(readOnly = true)
    public List<Task> searchTasksByTitle(String keyword, int page, int size) {
        UUID userId = userService.getCurrentUser().getId();
        return taskRepository
                .searchTasksByTitle(userId, keyword, PageRequest.of(page, size))
                .getContent();
    }

    @Transactional
    public Task updateTask(UUID taskId, UpdateTaskRequest dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("id", taskId));

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
            Category category = categoryRepository.findById(dto.getCategoryId())
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

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("id", dto.getCategoryId()));
        task.setCategory(category);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
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
                userService.getCurrentUser().getId(),
                PageRequest.of(0, 5)
        );
    }
}