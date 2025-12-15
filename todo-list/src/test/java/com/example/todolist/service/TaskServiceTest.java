package com.example.todolist.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import java.time.LocalDateTime;
import java.util.*;
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
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock TaskRepository taskRepository;
  @Mock CategoryRepository categoryRepository;
  @Mock UserRepository userRepository;
  @Mock UserService userService;

  @InjectMocks TaskService taskService;

  User mockUser;
  UUID userId;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    userId = UUID.randomUUID();
    mockUser.setId(userId);
  }

  private Task createTask(UUID id) {
    Task t = new Task();
    t.setId(id);
    t.setUser(mockUser);
    return t;
  }

  private Category createCategory(UUID id) {
    Category c = new Category();
    c.setId(id);
    return c;
  }

  // ------------------------------
  // getAllTasks
  // ------------------------------

  @Test
  void getAllTasks_ShouldReturnAllTasksOfUser() {
    List<Task> tasks = List.of(new Task(), new Task());
    when(taskRepository.findAllByUserId(userId)).thenReturn(tasks);
    when(userService.getCurrentUser()).thenReturn(mockUser);

    List<Task> result = taskService.getAllTasks();

    assertEquals(2, result.size());
    verify(taskRepository).findAllByUserId(userId);
  }

  // ------------------------------
  // findTaskById
  // ------------------------------

  @Test
  void findTaskById_ShouldReturnTask_WhenExists() {
    UUID id = UUID.randomUUID();
    Task task = createTask(id);

    when(taskRepository.findById(id)).thenReturn(Optional.of(task));

    Task result = taskService.findTaskById(id);

    assertEquals(id, result.getId());
  }

  @Test
  void findTaskById_ShouldThrow_WhenNotFound() {
    UUID id = UUID.randomUUID();
    when(taskRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(TaskNotFoundException.class, () -> taskService.findTaskById(id));
  }

  // ------------------------------
  // searchTasksByTitle
  // ------------------------------

  @Test
  void searchTasksByTitle_ShouldReturnFilteredTasks() {
    String keyword = "abc";
    PageRequest page = PageRequest.of(0, 5);
    Task task = new Task();
    Page<Task> mockPage = new PageImpl<>(List.of(task));
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByTitle(userId, keyword, page)).thenReturn(mockPage);

    List<Task> result = taskService.searchTasksByTitle(keyword, 0, 5);

    assertEquals(1, result.size());
    verify(taskRepository).searchTasksByTitle(userId, keyword, page);
  }

  // ------------------------------
  // deleteTaskById
  // ------------------------------

  @Test
  void deleteTaskById_ShouldCallRepository() {
    UUID taskId = UUID.randomUUID();

    taskService.deleteTaskById(taskId);

    verify(taskRepository, times(1)).deleteById(taskId);
  }

  // ------------------------------
  // updateTask
  // ------------------------------

  @Test
  void updateTask_ShouldUpdateAllProvidedFields() {
    UUID taskId = UUID.randomUUID();
    UUID catId = UUID.randomUUID();

    Task task = createTask(taskId);
    Category category = createCategory(catId);

    UpdateTaskRequest dto =
        new UpdateTaskRequest(
            "newTitle", "newDesc", Status.IN_PROGRESS, LocalDateTime.now(), catId);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(categoryRepository.findById(catId)).thenReturn(Optional.of(category));
    when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Task result = taskService.updateTask(taskId, dto);

    assertEquals("newTitle", result.getTitle());
    assertEquals("newDesc", result.getDescription());
    assertEquals(Status.IN_PROGRESS, result.getStatus());
    assertEquals(category, result.getCategory());
  }

  @Test
  void updateTask_ShouldKeepOldValues_WhenFieldsAreNull() {
    UUID taskId = UUID.randomUUID();
    Task task = createTask(taskId);

    task.setTitle("old");
    task.setDescription("oldDesc");
    task.setStatus(Status.TODO);

    UpdateTaskRequest dto =
        new UpdateTaskRequest(
            "new", // title zmieniamy
            null, // description zostaje
            null, // status zostaje
            null, // due date zostaje
            null // category zostaje
            );

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Task result = taskService.updateTask(taskId, dto);

    assertEquals("new", result.getTitle());
    assertEquals("oldDesc", result.getDescription());
    assertEquals(Status.TODO, result.getStatus());
  }

  @Test
  void updateTask_ShouldThrowWhenTaskNotFound() {
    UUID taskId = UUID.randomUUID();
    UpdateTaskRequest dto = new UpdateTaskRequest("a", null, null, null, null);

    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(taskId, dto));
  }

  @Test
  void updateTask_ShouldThrowWhenCategoryNotExists() {
    UUID taskId = UUID.randomUUID();
    UUID catId = UUID.randomUUID();

    Task task = createTask(taskId);
    UpdateTaskRequest dto = new UpdateTaskRequest("a", null, null, null, catId);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(categoryRepository.findById(catId)).thenReturn(Optional.empty());

    assertThrows(CategoryNotFoundException.class, () -> taskService.updateTask(taskId, dto));
  }

  // ------------------------------
  // createTask
  // ------------------------------

  @Test
  void createTask_ShouldCreateTaskProperly() {
    // given security context
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("user@mail.com", null));

    User user = new User();
    user.setEmail("user@mail.com");

    UUID catId = UUID.randomUUID();
    Category category = createCategory(catId);

    CreateTaskRequest dto =
        new CreateTaskRequest("title", "desc", Status.TODO, LocalDateTime.now(), catId);

    when(categoryRepository.findById(catId)).thenReturn(Optional.of(category));
    when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
    when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Task result = taskService.createTask(dto);

    assertEquals("title", result.getTitle());
    assertEquals("desc", result.getDescription());
    assertEquals(user, result.getUser());
    assertEquals(category, result.getCategory());
  }

  @Test
  void createTask_ShouldThrow_WhenCategoryNotFound() {
    UUID catId = UUID.randomUUID();

    CreateTaskRequest dto =
        new CreateTaskRequest("x", "y", Status.TODO, LocalDateTime.now(), catId);

    when(categoryRepository.findById(catId)).thenReturn(Optional.empty());

    assertThrows(CategoryNotFoundException.class, () -> taskService.createTask(dto));
  }

  // ------------------------------
  // getStats
  // ------------------------------

  @Test
  void getStats_ShouldReturnCounts() {
    when(taskRepository.countByUserId(userId)).thenReturn(10L);
    when(taskRepository.countByUserIdAndStatus(userId, Status.TODO)).thenReturn(4L);
    when(taskRepository.countByUserIdAndStatus(userId, Status.IN_PROGRESS)).thenReturn(3L);
    when(taskRepository.countByUserIdAndStatus(userId, Status.DONE)).thenReturn(3L);
    when(userService.getCurrentUser()).thenReturn(mockUser);

    Map<String, Long> stats = taskService.getStats();

    assertEquals(10L, stats.get("totalTasks"));
    assertEquals(4L, stats.get("todoTasks"));
    assertEquals(3L, stats.get("inProgressTasks"));
    assertEquals(3L, stats.get("doneTasks"));
  }

  // ------------------------------
  // getUpcomingTasks
  // ------------------------------

  @Test
  void getUpcomingTasks_ShouldReturnPage() {
    Page<Task> page = new PageImpl<>(List.of(new Task()));
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.findByUserIdAndDueDateIsNotNullOrderByDueDateAsc(eq(userId), any()))
        .thenReturn(page);

    Page<Task> result = taskService.getUpcomingTasks();

    assertEquals(1, result.getContent().size());
  }
}
