package com.example.todolist.service;

import com.example.todolist.dto.UpdateTaskRequest;
import com.example.todolist.dto.CreateTaskRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    TaskRepository taskRepository;

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    TaskService taskService;

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        Task task1 = new Task();
        task1.setId(UUID.randomUUID());
        task1.setTitle("Task 1");

        Task task2 = new Task();
        task2.setId(UUID.randomUUID());
        task2.setTitle("Task 2");

        List<Task> tasks = List.of(task1, task2);

        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());

        verify(taskRepository).findAll();
    }


    @Test
    void getTaskById_ShouldReturnTaskById() {
        UUID id = UUID.randomUUID();

        Task task = new Task();
        task.setId(id);
        task.setTitle("Test Task");

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        Task result = taskService.findTaskById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Task", result.getTitle());

        verify(taskRepository).findById(id);
    }

    @Test
    void getTaskById_ShouldThrowExceptionWhenTaskNotFound() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.findTaskById(id));

        verify(taskRepository).findById(id);
    }


    @Test
    void searchTasksByTitle_ShouldReturnMatchingTasks() {
        String keyword = "Test";
        int page = 0;
        int size = 5;

        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Test Task");

        when(taskRepository.searchTasksByTitle(keyword, PageRequest.of(page, size)))
                .thenReturn(List.of(task));

        List<Task> result = taskService.searchTasksByTitle(keyword, page, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());

        verify(taskRepository).searchTasksByTitle(keyword, PageRequest.of(page, size));
    }


    @Test
    void deleteTaskById_ShouldDeleteTaskById() {
        UUID id = UUID.randomUUID();

        taskService.deleteTaskById(id);

        verify(taskRepository).deleteById(id);
    }


    @Test
    void updateTask_ShouldUpdateTask() {
        UUID taskId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Old");

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Work");

        UpdateTaskRequest dto = new UpdateTaskRequest(
                "New Title",
                "New Desc",
                com.example.todolist.entity.Status.IN_PROGRESS,
                LocalDateTime.now(),
                categoryId
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task result = taskService.updateTask(taskId, dto);

        assertEquals("New Title", result.getTitle());
        assertEquals("New Desc", result.getDescription());
        assertEquals(com.example.todolist.entity.Status.IN_PROGRESS, result.getStatus());
        assertEquals(category, result.getCategory());

        verify(taskRepository).findById(taskId);
        verify(categoryRepository).findById(categoryId);
        verify(taskRepository).save(existingTask);
    }

    @Test
    void updateTask_ShouldUpdateDescription_WhenDescriptionIsNotNull() {
        UUID taskId = UUID.randomUUID();

        Task task = new Task();
        task.setId(taskId);
        task.setDescription("old");

        UpdateTaskRequest dto = new UpdateTaskRequest(
                "title",
                "new description",
                null,
                null,
                null
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(taskId, dto);

        assertEquals("new description", result.getDescription());
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_ShouldUpdateStatus_WhenStatusIsNotNull() {
        UUID taskId = UUID.randomUUID();

        Task task = new Task();
        task.setId(taskId);
        task.setStatus(Status.TODO);

        UpdateTaskRequest dto = new UpdateTaskRequest(
                "title",
                null,
                Status.IN_PROGRESS,
                null,
                null
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(taskId, dto);

        assertEquals(Status.IN_PROGRESS, result.getStatus());
    }

    @Test
    void updateTask_ShouldUpdateDueDate_WhenDueDateIsNotNull() {
        UUID taskId = UUID.randomUUID();
        LocalDateTime newDate = LocalDateTime.now();

        Task task = new Task();
        task.setId(taskId);
        task.setDueDate(null);

        UpdateTaskRequest dto = new UpdateTaskRequest(
                "title",
                null,
                null,
                newDate,
                null
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(taskId, dto);

        assertEquals(newDate, result.getDueDate());
    }

    @Test
    void updateTask_ShouldUpdateCategory_WhenCategoryIdIsNotNull() {
        UUID taskId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Task task = new Task();
        task.setId(taskId);

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Work");

        UpdateTaskRequest dto = new UpdateTaskRequest(
                "title",
                null,
                null,
                null,
                categoryId
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(taskId, dto);

        assertEquals(category, result.getCategory());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void updateTask_ShouldThrowExceptionWhenTaskNotFound() {
        UUID taskId = UUID.randomUUID();

        UpdateTaskRequest dto = new UpdateTaskRequest(
                "title",
                null,
                null,
                null,
                null
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> taskService.updateTask(taskId, dto));

        verify(taskRepository).findById(taskId);
    }


    @Test
    void updateTask_ShouldThrowExceptionWhenCategoryIdNotFound() {
        UUID taskId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Task task = new Task();
        task.setId(taskId);

        UpdateTaskRequest dto = new UpdateTaskRequest(
                "Title",
                null,
                null,
                null,
                categoryId
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> taskService.updateTask(taskId, dto));
    }

    @Test
    void createTask_ShouldCreateTask() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Work");

        CreateTaskRequest dto = new CreateTaskRequest(
                "New",
                "Description",
                com.example.todolist.entity.Status.TODO,
                LocalDateTime.now(),
                categoryId
        );

        Task savedTask = new Task();
        savedTask.setId(UUID.randomUUID());
        savedTask.setTitle("New");
        savedTask.setCategory(category);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        Task result = taskService.createTask(dto);

        assertNotNull(result);
        assertEquals("New", result.getTitle());
        assertEquals(category, result.getCategory());

        verify(categoryRepository).findById(categoryId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowExceptionWhenCategoryIsNull() {
        UUID categoryId = UUID.randomUUID();

        CreateTaskRequest dto = new CreateTaskRequest(
                "task",
                "desc",
                Status.TODO,
                LocalDateTime.now(),
                categoryId
        );

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> taskService.createTask(dto));

        verify(categoryRepository).findById(categoryId);
    }
}
