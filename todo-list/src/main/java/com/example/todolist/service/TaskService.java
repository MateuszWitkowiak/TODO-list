package com.example.todolist.service;

import com.example.todolist.entity.Task;
import com.example.todolist.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// get (lista z paginacją, single), post, put (aktualizacja), delete
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void deleteTaskById(UUID taskId) {
        taskRepository.deleteById(taskId);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task findTaskById(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(null);
    }
    public List<Task> searchTasksByTitle(String keyword, int page, int size) {
        return taskRepository.searchTasksByTitle(keyword, PageRequest.of(page, size));
    }
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(UUID taskId) {
        Task task = findTaskById(taskId).orElseThrow(() -> new RuntimeException("Task not found."));
    }
}
