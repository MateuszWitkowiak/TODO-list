package com.example.todolist.service;

import static org.junit.jupiter.api.Assertions.*;
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
import com.example.todolist.service.filter.TaskFilter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService")
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
    SecurityContextHolder.clearContext();
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

  @Test
  @DisplayName("getAllTasks should return all tasks of current user")
  void getAllTasks_ShouldReturnAllTasksOfUser() {
    List<Task> tasks = List.of(new Task(), new Task());
    when(taskRepository.findAllByUserId(userId)).thenReturn(tasks);
    when(userService.getCurrentUser()).thenReturn(mockUser);

    List<Task> result = taskService.getAllTasks();

    assertEquals(2, result.size());
    verify(taskRepository).findAllByUserId(userId);
  }

  @Test
  @DisplayName("Returns tasks with default sort and page/size normalization")
  void getAllTasks_DefaultsPaginationAndSort() {
    TaskFilter filter = new TaskFilter();
    filter.setPage(-5);
    filter.setSize(0);
    filter.setSort(null);
    filter.setDirection(null);
    when(userService.getCurrentUser()).thenReturn(mockUser);
    Page<Task> expected = new PageImpl<>(List.of());
    when(taskRepository.searchTasksByFilter(
            eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
        .thenReturn(expected);

    Page<Task> result = taskService.getAllTasks(filter);

    assertSame(expected, result);

    ArgumentCaptor<PageRequest> prCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(taskRepository)
        .searchTasksByFilter(
            eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), prCaptor.capture());
    PageRequest pageRequest = prCaptor.getValue();
    assertEquals(0, pageRequest.getPageNumber());
    assertEquals(1, pageRequest.getPageSize());
    assertEquals(Sort.by("title").getOrderFor("title").getDirection(), Sort.Direction.ASC);
  }

  @Test
  @DisplayName("Handles descending sort and custom sort property")
  void getAllTasks_DescendingSort() {

    TaskFilter filter = new TaskFilter();
    filter.setSort("someField");
    filter.setDirection("desc");
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByFilter(
            eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
        .thenReturn(Page.empty());

    taskService.getAllTasks(filter);

    ArgumentCaptor<PageRequest> prCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(taskRepository)
        .searchTasksByFilter(any(), any(), any(), any(), any(), any(), prCaptor.capture());
    PageRequest pageRequest = prCaptor.getValue();
    assertEquals(
        Sort.Direction.DESC,
        Objects.requireNonNull(pageRequest.getSort().getOrderFor("someField")).getDirection());
  }

  @Test
  @DisplayName("Handles date filters")
  void getAllTasks_WithDueAfterAndDueBefore() {
    TaskFilter filter = new TaskFilter();
    filter.setDueAfter(LocalDate.of(2023, 1, 15));
    filter.setDueBefore(LocalDate.of(2023, 2, 5));
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByFilter(
            eq(userId),
            isNull(),
            isNull(),
            isNull(),
            eq(LocalDate.of(2023, 1, 15).atStartOfDay()),
            eq(LocalDate.of(2023, 2, 5).atTime(LocalTime.MAX)),
            any(PageRequest.class)))
        .thenReturn(Page.empty());

    taskService.getAllTasks(filter);

    verify(taskRepository)
        .searchTasksByFilter(
            eq(userId),
            isNull(),
            isNull(),
            isNull(),
            eq(LocalDate.of(2023, 1, 15).atStartOfDay()),
            eq(LocalDate.of(2023, 2, 5).atTime(LocalTime.MAX)),
            any(PageRequest.class));
  }

  @Test
  @DisplayName("Handles valid status filter")
  void getAllTasks_WithValidStatus() {
    TaskFilter filter = new TaskFilter();
    filter.setStatus("TODO");
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByFilter(
            eq(userId),
            isNull(),
            eq(Status.TODO),
            isNull(),
            isNull(),
            isNull(),
            any(PageRequest.class)))
        .thenReturn(Page.empty());

    taskService.getAllTasks(filter);

    verify(taskRepository)
        .searchTasksByFilter(
            eq(userId),
            isNull(),
            eq(Status.TODO),
            isNull(),
            isNull(),
            isNull(),
            any(PageRequest.class));
  }

  @Test
  @DisplayName("Handles invalid status and ignores it")
  void getAllTasks_WithInvalidStatus() {
    TaskFilter filter = new TaskFilter();
    filter.setStatus("NOT_A_STATUS");
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByFilter(
            eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
        .thenReturn(Page.empty());

    taskService.getAllTasks(filter);

    verify(taskRepository)
        .searchTasksByFilter(
            eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class));
  }

  @Test
  @DisplayName("Handles categoryId filter")
  void getAllTasks_WithCategoryId() {
    TaskFilter filter = new TaskFilter();
    UUID categoryId = UUID.randomUUID();
    filter.setCategoryId(categoryId);
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByFilter(
            eq(userId),
            isNull(),
            isNull(),
            eq(categoryId),
            isNull(),
            isNull(),
            any(PageRequest.class)))
        .thenReturn(Page.empty());

    taskService.getAllTasks(filter);

    verify(taskRepository)
        .searchTasksByFilter(
            eq(userId),
            isNull(),
            isNull(),
            eq(categoryId),
            isNull(),
            isNull(),
            any(PageRequest.class));
  }

  @Test
  @DisplayName("findTaskById should return task when exists")
  void findTaskById_ShouldReturnTask_WhenExists() {
    UUID id = UUID.randomUUID();
    Task task = createTask(id);

    when(taskRepository.findById(id)).thenReturn(Optional.of(task));

    Task result = taskService.findTaskById(id);

    assertEquals(id, result.getId());
  }

  @Test
  @DisplayName("findTaskById should throw TaskNotFoundException when not found")
  void findTaskById_ShouldThrow_WhenNotFound() {
    UUID id = UUID.randomUUID();
    when(taskRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(TaskNotFoundException.class, () -> taskService.findTaskById(id));
  }

  @Test
  @DisplayName("searchTasksByTitle should return filtered tasks")
  void searchTasksByTitle_ShouldReturnFilteredTasks() {
    String keyword = "abc";
    int pageNum = 0;
    int size = 5;
    PageRequest pageRequest = PageRequest.of(pageNum, size, Sort.by("title").ascending());
    Task task = new Task();
    Page<Task> mockPage = new PageImpl<>(List.of(task));

    when(userService.getCurrentUser()).thenReturn(mockUser);

    TaskFilter filter = new TaskFilter();
    filter.setTitle(keyword);
    filter.setPage(pageNum);
    filter.setSize(size);

    when(taskRepository.searchTasksByFilter(userId, keyword, null, null, null, null, pageRequest))
        .thenReturn(mockPage);

    Page<Task> result = taskService.searchTasksByTitle(filter);

    assertEquals(1, result.getTotalElements());
    verify(taskRepository)
        .searchTasksByFilter(userId, keyword, null, null, null, null, pageRequest);
  }

  @Test
  @DisplayName("Keyword blank/null -> empty string as keyword ")
  void keywordIsBlankOrNull_TreatedAsEmptyString() {
    TaskFilter filterNull = new TaskFilter();
    filterNull.setTitle(null);
    filterNull.setPage(0);
    filterNull.setSize(1);
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByFilter(any(), eq(""), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());
    taskService.searchTasksByTitle(filterNull);

    TaskFilter filterBlank = new TaskFilter();
    filterBlank.setTitle("  ");
    filterBlank.setPage(0);
    filterBlank.setSize(1);
    when(taskRepository.searchTasksByFilter(any(), eq(""), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());
    taskService.searchTasksByTitle(filterBlank);

    verify(taskRepository, times(2))
        .searchTasksByFilter(any(), eq(""), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("Sort property fallback to title, direction fallback to ASC, custom DESC sort")
  void sortPropertyAndSortDirection_AllReferences() {
    TaskFilter filter = new TaskFilter();
    filter.setSort(null);
    filter.setDirection(null);
    filter.setTitle("X");
    filter.setPage(0);
    filter.setSize(1);
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByFilter(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());
    taskService.searchTasksByTitle(filter);

    filter.setSort(" ");
    taskService.searchTasksByTitle(filter);

    filter.setSort("deadline");
    filter.setDirection("desc");
    taskService.searchTasksByTitle(filter);

    ArgumentCaptor<PageRequest> prCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(taskRepository, atLeastOnce())
        .searchTasksByFilter(any(), any(), any(), any(), any(), any(), prCaptor.capture());
    boolean descCaptured =
        prCaptor.getAllValues().stream()
            .anyMatch(
                pr ->
                    pr.getSort().stream()
                        .anyMatch(
                            o ->
                                o.getProperty().equals("deadline")
                                    && o.getDirection() == Sort.Direction.DESC));
    assertTrue(descCaptured);
  }

  @Test
  @DisplayName("dueAfter and dueBefore passed correctly (incl. null)")
  void dueAfterDueBeforeAreProcessed() {
    TaskFilter filter = new TaskFilter();
    filter.setTitle("a");
    filter.setPage(0);
    filter.setSize(1);
    when(userService.getCurrentUser()).thenReturn(mockUser);
    taskService.searchTasksByTitle(filter);

    filter.setDueAfter(LocalDate.of(2024, 2, 1));
    taskService.searchTasksByTitle(filter);

    filter.setDueAfter(null);
    filter.setDueBefore(LocalDate.of(2024, 2, 20));
    taskService.searchTasksByTitle(filter);

    filter.setDueAfter(LocalDate.of(2024, 2, 1));
    filter.setDueBefore(LocalDate.of(2024, 2, 20));
    taskService.searchTasksByTitle(filter);

    verify(taskRepository, times(4))
        .searchTasksByFilter(eq(userId), any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("Valid status is mapped, invalid triggers catch block and is passed as null")
  void status_MappingAndCatch() {
    TaskFilter filter = new TaskFilter();
    filter.setPage(0);
    filter.setSize(1);
    filter.setTitle("abc");

    filter.setStatus("TODO");
    when(userService.getCurrentUser()).thenReturn(mockUser);
    taskService.searchTasksByTitle(filter);
    verify(taskRepository)
        .searchTasksByFilter(eq(userId), any(), eq(Status.TODO), any(), any(), any(), any());

    reset(taskRepository);
    filter.setStatus("ASDFG");
    taskService.searchTasksByTitle(filter);
    verify(taskRepository)
        .searchTasksByFilter(eq(userId), any(), isNull(), any(), any(), any(), any());

    reset(taskRepository);
    filter.setStatus(null);
    taskService.searchTasksByTitle(filter);
    verify(taskRepository)
        .searchTasksByFilter(eq(userId), any(), isNull(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("Pagination is normalized for negative/zero")
  void pagination_IsNormalized() {
    TaskFilter filter = new TaskFilter();
    filter.setTitle("abc");
    filter.setPage(-10);
    filter.setSize(0);
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.searchTasksByFilter(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());
    taskService.searchTasksByTitle(filter);

    ArgumentCaptor<PageRequest> prCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(taskRepository)
        .searchTasksByFilter(any(), any(), any(), any(), any(), any(), prCaptor.capture());
    PageRequest req = prCaptor.getValue();
    assertEquals(0, req.getPageNumber());
    assertEquals(1, req.getPageSize());
  }

  @Test
  @DisplayName("deleteTaskById should call repository to delete task")
  void deleteTaskById_ShouldCallRepository() {
    UUID taskId = UUID.randomUUID();

    taskService.deleteTaskById(taskId);

    verify(taskRepository, times(1)).deleteById(taskId);
  }

  @Test
  @DisplayName("updateTask should update all provided fields")
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
  @DisplayName("updateTask should keep old values when fields are null")
  void updateTask_ShouldKeepOldValues_WhenFieldsAreNull() {
    UUID taskId = UUID.randomUUID();
    Task task = createTask(taskId);

    task.setTitle("old");
    task.setDescription("oldDesc");
    task.setStatus(Status.TODO);

    UpdateTaskRequest dto = new UpdateTaskRequest("new", null, null, null, null);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Task result = taskService.updateTask(taskId, dto);

    assertEquals("new", result.getTitle());
    assertEquals("oldDesc", result.getDescription());
    assertEquals(Status.TODO, result.getStatus());
  }

  @Test
  @DisplayName("updateTask should throw TaskNotFoundException when task not found")
  void updateTask_ShouldThrowWhenTaskNotFound() {
    UUID taskId = UUID.randomUUID();
    UpdateTaskRequest dto = new UpdateTaskRequest("a", null, null, null, null);

    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(taskId, dto));
  }

  @Test
  @DisplayName("updateTask should throw CategoryNotFoundException when category does not exist")
  void updateTask_ShouldThrowWhenCategoryNotExists() {
    UUID taskId = UUID.randomUUID();
    UUID catId = UUID.randomUUID();

    Task task = createTask(taskId);
    UpdateTaskRequest dto = new UpdateTaskRequest("a", null, null, null, catId);

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(categoryRepository.findById(catId)).thenReturn(Optional.empty());

    assertThrows(CategoryNotFoundException.class, () -> taskService.updateTask(taskId, dto));
  }

  @Test
  @DisplayName("createTask should create a task with correct properties")
  void createTask_ShouldCreateTaskProperly() {
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
  @DisplayName("createTask should throw CategoryNotFoundException when category not found")
  void createTask_ShouldThrow_WhenCategoryNotFound() {
    UUID catId = UUID.randomUUID();

    CreateTaskRequest dto =
        new CreateTaskRequest("x", "y", Status.TODO, LocalDateTime.now(), catId);

    when(categoryRepository.findById(catId)).thenReturn(Optional.empty());

    assertThrows(CategoryNotFoundException.class, () -> taskService.createTask(dto));
  }

  @Test
  @DisplayName("getStats should return correct counts for tasks")
  void getStats_ShouldReturnCounts() {
    when(taskRepository.countByUserId(userId)).thenReturn(10L);
    when(taskRepository.countByUserIdAndStatus(userId, Status.TODO)).thenReturn(4L);
    when(taskRepository.countByUserIdAndStatus(userId, Status.IN_PROGRESS)).thenReturn(3L);
    when(taskRepository.countByUserIdAndStatus(userId, Status.DONE)).thenReturn(3L);
    when(userService.getCurrentUser()).thenReturn(mockUser);

    Map<String, Object> stats = taskService.getStats();

    assertEquals(10L, stats.get("totalTasks"));
    assertEquals(4L, stats.get("todoTasks"));
    assertEquals(3L, stats.get("inProgressTasks"));
    assertEquals(3L, stats.get("doneTasks"));
  }

  @Test
  @DisplayName("getUpcomingTasks should return tasks with nearest due dates")
  void getUpcomingTasks_ShouldReturnPage() {
    Page<Task> page = new PageImpl<>(List.of(new Task()));
    when(userService.getCurrentUser()).thenReturn(mockUser);
    when(taskRepository.findByUserIdAndDueDateIsNotNullOrderByDueDateAsc(eq(userId), any()))
        .thenReturn(page);

    Page<Task> result = taskService.getUpcomingTasks();

    assertEquals(1, result.getContent().size());
  }
}
