package com.example.todolist.service;

import com.example.todolist.dto.CreateTaskRequest;
import com.example.todolist.dto.UpdateTaskRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
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

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void deleteTaskById(UUID taskId) {
        taskRepository.deleteById(taskId);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Task findTaskById(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Id", taskId));
    }

    @Transactional(readOnly = true)
    public List<Task> searchTasksByTitle(String keyword, int page, int size) {
        return taskRepository.searchTasksByTitle(keyword, PageRequest.of(page, size));
    }

    @Transactional
    public Task updateTask(UUID taskId, UpdateTaskRequest dto) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("id", taskId));

        task.setTitle(dto.title());
        if (dto.description() != null) task.setDescription(dto.description());
        if (dto.status() != null) task.setStatus(dto.status());
        if (dto.dueDate() != null) task.setDueDate(dto.dueDate());
        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId()).orElseThrow(() -> new CategoryNotFoundException("id", dto.categoryId()));
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

        return taskRepository.save(task);
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();

        long total = taskRepository.count();
        long todo = taskRepository.countByStatus(Status.TODO);
        long inProgress = taskRepository.countByStatus(Status.IN_PROGRESS);
        long done = taskRepository.countByStatus(Status.DONE);

        stats.put("totalTasks", total);
        stats.put("todoTasks", todo);
        stats.put("inProgressTasks", inProgress);
        stats.put("doneTasks", done);

        return stats;
    }

    public List<Task> getUpcomingTasks() {
        return taskRepository.findTop5ByDueDateIsNotNullOrderByDueDateAsc();
    }

}
