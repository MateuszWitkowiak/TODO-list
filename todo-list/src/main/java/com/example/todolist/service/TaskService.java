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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository, UserRepository userRepository, UserService userService) {
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
    public Task findTaskById(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Id", taskId));
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
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("id", taskId));

        task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new CategoryNotFoundException("id", dto.getCategoryId()));
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

        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new CategoryNotFoundException("id", dto.getCategoryId()));
        task.setCategory(category);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        task.setUser(user);

        return taskRepository.save(task);
    }

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

    public Page<Task> getUpcomingTasks() {
        return taskRepository.findByUserIdAndDueDateIsNotNullOrderByDueDateAsc(userService.getCurrentUser().getId(), PageRequest.of(0, 5));
    }
}
