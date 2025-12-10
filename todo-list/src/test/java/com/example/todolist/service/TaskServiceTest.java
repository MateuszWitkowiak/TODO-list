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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @InjectMocks
    TaskService taskService;

    User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
    }

    @Test
    void getAllTasks_ShouldReturnTasksForUser() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        Task t1 = new Task();
        Task t2 = new Task();

        when(taskRepository.findAllByUserId(mockUser.getId()))
                .thenReturn(List.of(t1, t2));

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        verify(taskRepository).findAllByUserId(mockUser.getId());
    }

    @Test
    void getTaskById_ShouldReturnTask() {
        UUID id = UUID.randomUUID();
        Task task = new Task();
        task.setId(id);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        Task result = taskService.findTaskById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void getTaskById_ShouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.findTaskById(id));
    }

    @Test
    void searchTasksByTitle_ShouldReturnFilteredTasks() {
        when(userService.getCurrentUser()).thenReturn(mockUser);
        String keyword = "test";
        PageRequest page = PageRequest.of(0, 5);

        Task task = new Task();
        Page<Task> pageResult = new PageImpl<>(List.of(task));

        when(taskRepository.searchTasksByTitle(mockUser.getId(), keyword, page))
                .thenReturn(pageResult);

        List<Task> result = taskService.searchTasksByTitle(keyword, 0, 5);

        assertEquals(1, result.size());
        verify(taskRepository).searchTasksByTitle(mockUser.getId(), keyword, page);
    }

    @Test
    void deleteTaskById_ShouldCallRepository() {
        UUID id = UUID.randomUUID();

        taskService.deleteTaskById(id);

        verify(taskRepository).deleteById(id);
    }

    @Test
    void updateTask_ShouldUpdateFields() {
        UUID taskId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Task task = new Task();
        task.setId(taskId);

        Category category = new Category();
        category.setId(categoryId);

        UpdateTaskRequest dto = new UpdateTaskRequest(
                "new",
                "desc",
                Status.IN_PROGRESS,
                LocalDateTime.now(),
                categoryId
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(taskId, dto);

        assertEquals("new", result.getTitle());
        assertEquals("desc", result.getDescription());
        assertEquals(Status.IN_PROGRESS, result.getStatus());
        assertEquals(category, result.getCategory());
    }

    @Test
    void updateTask_ShouldThrowWhenTaskNotFound() {
        UUID id = UUID.randomUUID();

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        UpdateTaskRequest dto = new UpdateTaskRequest("a", null, null, null, null);

        assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(id, dto));
    }

    @Test
    void updateTask_ShouldThrowWhenCategoryNotFound() {
        UUID taskId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Task task = new Task();
        task.setId(taskId);

        UpdateTaskRequest dto = new UpdateTaskRequest("a", null, null, null, categoryId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> taskService.updateTask(taskId, dto));
    }

    @Test
    void createTask_ShouldCreateTask() {
        // security context
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("email@test.com", null);
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);

        // user
        User user = new User();
        user.setEmail("email@test.com");

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));

        // category
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CreateTaskRequest dto = new CreateTaskRequest(
                "title",
                "desc",
                Status.TODO,
                LocalDateTime.now(),
                categoryId
        );

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setCategory(category);
        saved.setUser(user);

        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        Task result = taskService.createTask(dto);

        assertNotNull(result);
        assertEquals(category, result.getCategory());
        assertEquals(user, result.getUser());
    }

    @Test
    void createTask_ShouldThrowWhenCategoryMissing() {
        UUID categoryId = UUID.randomUUID();

        CreateTaskRequest dto = new CreateTaskRequest(
                "x", "y", Status.TODO, LocalDateTime.now(), categoryId
        );

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> taskService.createTask(dto));
    }
}
