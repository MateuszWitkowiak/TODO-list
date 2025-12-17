package com.example.todolist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService")
class TaskServiceTest {

  @Mock TaskRepository taskRepository;
  @Mock CategoryRepository categoryRepository;
  @Mock UserRepository userRepository;
  @Mock UserService userService;
  @Mock Validator validator;

  @InjectMocks TaskService taskService;

  User mockUser;
  UUID userId;
  ByteArrayOutputStream outStream;

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

  private Task createTask(
      UUID id,
      String title,
      String description,
      Status status,
      LocalDateTime dueDate,
      Category category,
      User user) {
    Task t = new Task();
    t.setId(id);
    t.setTitle(title);
    t.setDescription(description);
    t.setStatus(status);
    t.setDueDate(dueDate);
    t.setUser(mockUser);
    t.setCategory(category);
    t.setUser(user);
    return t;
  }

  private Category createCategory(UUID id) {
    Category c = new Category();
    c.setId(id);
    return c;
  }

  @Nested
  @DisplayName("GetAllTasks")
  class GetAllTasksTests {
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
  }

  @Nested
  @DisplayName("GetTaskById")
  class GetTaskByIdTests {
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
  }

  @Nested
  @DisplayName("SearchTasksByTitle")
  class SearchTasksByTitleTests {
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
  }

  @Nested
  @DisplayName("DeleteTasks")
  class DeleteTasksTests {
    @Test
    @DisplayName("deleteTaskById should call repository to delete task")
    void deleteTaskById_ShouldCallRepository() {
      UUID taskId = UUID.randomUUID();

      taskService.deleteTaskById(taskId);

      verify(taskRepository, times(1)).deleteById(taskId);
    }
  }

  @Nested
  @DisplayName("UpdateTasks")
  class UpdateTasksTests {

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
  }

  @Nested
  @DisplayName("CreateTasks")
  class CreateTasksTests {
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
  }

  @Nested
  @DisplayName("GetStats")
  class GetStatsTests {
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
  }

  @Nested
  @DisplayName("GetUpcomingTasks")
  class GetUpcomingTasksTests {
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

  @Nested
  @DisplayName("Export")
  class ExportTests {
    @Test
    @DisplayName("Should export tasks to CSV with correct UTF-8 content and header")
    void exportTasksCsv_success() throws Exception {
      User user = new User();
      UUID userId = UUID.randomUUID();
      user.setId(userId);

      Category cat = new Category();
      cat.setName("Praca");

      LocalDateTime date = LocalDateTime.of(2025, 12, 31, 18, 30);

      List<Task> tasks =
          List.of(
              createTask(UUID.randomUUID(), "żółw", "ąćęłńóśźż", Status.TODO, date, cat, user),
              createTask(UUID.randomUUID(), null, null, null, null, null, user));
      when(userService.getCurrentUser()).thenReturn(user);
      when(taskRepository.findAllByUserId(userId)).thenReturn(tasks);

      HttpServletResponse response = mock(HttpServletResponse.class);

      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ServletOutputStream sos =
          new ServletOutputStream() {
            @Override
            public void write(int b) {
              outStream.write(b);
            }

            @Override
            public boolean isReady() {
              return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener listener) {}
          };
      when(response.getOutputStream()).thenReturn(sos);

      taskService.exportTasksToCSV(response);

      String result = outStream.toString(StandardCharsets.UTF_8);
      assertThat(result)
          .contains("title;description;status;dueDate;categoryName")
          .contains("żółw;ąćęłńóśźż;TODO;2025-12-31T18:30;Praca")
          .contains(";;;;");
      verify(response).setContentType("text/csv; charset=UTF-8");
      verify(response).setHeader("Content-Disposition", "attachment; filename=\"tasks.csv\"");
    }

    @Test
    @DisplayName("Should throw RuntimeException on writer error")
    void exportTasksCsv_writerError() throws Exception {
      User user = new User();
      user.setId(UUID.randomUUID());
      when(userService.getCurrentUser()).thenReturn(user);
      when(taskRepository.findAllByUserId(any())).thenThrow(new RuntimeException("DB error"));

      HttpServletResponse response = mock(HttpServletResponse.class);
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ServletOutputStream sos =
          new ServletOutputStream() {
            @Override
            public void write(int b) {
              outStream.write(b);
            }

            @Override
            public boolean isReady() {
              return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener listener) {}
          };

      assertThatThrownBy(() -> taskService.exportTasksToCSV(response))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("DB error");
    }
  }

  @Nested
  @DisplayName("Import")
  class ImportTests {
    @Test
    @DisplayName("Should import well-formed UTF-8 CSV and persist tasks properly")
    void importCsv_success() throws Exception {
      User user = new User();
      user.setId(UUID.randomUUID());
      when(userService.getCurrentUser()).thenReturn(user);

      Category cat = new Category();
      cat.setName("Praca");
      cat.setId(UUID.randomUUID());
      when(categoryRepository.findByNameAndUserId(eq("Praca"), any())).thenReturn(Optional.of(cat));
      when(categoryRepository.findById(eq(cat.getId()))).thenReturn(Optional.of(cat));

      String csv =
          "title;description;status;dueDate;categoryName\n"
              + "Aktualizacja dokumentacji;Dodać nowe endpointy do API docs;TODO;2025-12-14T12:00;Praca\n"
              + "Tytuł;Opis;DONE;2025-12-11T10:00;Praca\n"
              + "Kolejne;Coś;IN_PROGRESS;;\n";
      MockMultipartFile file =
          new MockMultipartFile(
              "file", "tasks.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

      taskService.importTasksFromCsv(file);

      ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
      verify(taskRepository, times(3)).save(captor.capture());
      List<Task> imported = captor.getAllValues();

      assertThat(imported.get(0))
          .extracting(
              Task::getTitle,
              Task::getDescription,
              Task::getStatus,
              Task::getDueDate,
              t -> t.getCategory() != null ? t.getCategory().getName() : null)
          .containsExactly(
              "Aktualizacja dokumentacji",
              "Dodać nowe endpointy do API docs",
              Status.TODO,
              LocalDateTime.parse("2025-12-14T12:00"),
              "Praca");
      assertThat(imported.get(1).getStatus()).isEqualTo(Status.DONE);
      assertThat(imported.get(2).getStatus()).isEqualTo(Status.IN_PROGRESS);
      assertThat(imported.get(2).getDueDate()).isNull();
    }

    @Test
    @DisplayName("Should import with unknown category gracefully (category=null)")
    void importCsv_unknownCategory() throws Exception {
      User user = new User();
      user.setId(UUID.randomUUID());
      when(userService.getCurrentUser()).thenReturn(user);
      when(categoryRepository.findByNameAndUserId(eq("Brak"), any())).thenReturn(Optional.empty());

      String csv =
          "title;description;status;dueDate;categoryName\n" + "A;B;DONE;2025-12-15T15:00;Brak\n";
      MockMultipartFile file =
          new MockMultipartFile(
              "file", "tasks.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

      taskService.importTasksFromCsv(file);

      ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
      verify(taskRepository).save(captor.capture());

      assertThat(captor.getValue().getCategory()).isNull();
    }

    @Test
    @DisplayName("Should treat missing/empty fields as null/task defaults")
    void importCsv_missingFields() throws Exception {
      User user = new User();
      user.setId(UUID.randomUUID());
      when(userService.getCurrentUser()).thenReturn(user);

      String csv = "title;description;status;dueDate;categoryName\n" + "Tylko tytul;;;;\n";
      MockMultipartFile file =
          new MockMultipartFile(
              "file", "tasks.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

      taskService.importTasksFromCsv(file);

      ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
      verify(taskRepository).save(captor.capture());

      Task task = captor.getValue();
      assertThat(task.getTitle()).isEqualTo("Tylko tytul");
      assertThat(task.getDescription()).isNull();
      assertThat(task.getStatus()).isEqualTo(Status.TODO);
      assertThat(task.getDueDate()).isNull();
      assertThat(task.getCategory()).isNull();
    }
  }
}
