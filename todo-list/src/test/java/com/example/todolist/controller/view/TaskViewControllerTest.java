package com.example.todolist.controller.view;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.service.CategoryService;
import com.example.todolist.service.TaskService;
import com.example.todolist.service.filter.TaskFilter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskViewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TaskViewController")
class TaskViewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TaskService taskService;

  @MockitoBean private CategoryService categoryService;

  @Test
  @DisplayName("GET /tasks/add should return add task form view")
  void showAddTaskForm_ShouldReturnAddView() throws Exception {
    when(categoryService.findAllCategories()).thenReturn(List.of());

    mockMvc
        .perform(get("/tasks/add"))
        .andExpect(status().isOk())
        .andExpect(view().name("task-add"))
        .andExpect(model().attributeExists("task"))
        .andExpect(model().attributeExists("categories"))
        .andExpect(model().attribute("isEdit", false));
  }

  @Test
  @DisplayName("POST /tasks with valid data should create task and redirect")
  void submitNewTask_ShouldCreateTaskAndRedirect() throws Exception {
    UUID catId = UUID.randomUUID();
    Category category = new Category();
    category.setId(catId);
    when(categoryService.findAllCategories()).thenReturn(List.of(category));

    mockMvc
        .perform(
            post("/tasks")
                .param("title", "Task title")
                .param("description", "Some desc")
                .param("status", "TODO")
                .param("categoryId", catId.toString())
                .param("dueDate", LocalDateTime.now().plusDays(1).withNano(0).toString())
                .contentType("application/x-www-form-urlencoded"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tasks"));

    verify(taskService).createTask(any(CreateTaskRequest.class));
  }

  @Test
  @DisplayName("POST /tasks with invalid data should return to form")
  void submitNewTask_WithInvalidData_ShouldReturnToAddForm() throws Exception {
    when(categoryService.findAllCategories()).thenReturn(List.of());

    mockMvc
        .perform(
            post("/tasks")
                .param("description", "desc")
                .contentType("application/x-www-form-urlencoded"))
        .andExpect(status().isOk())
        .andExpect(view().name("task-add"))
        .andExpect(model().attributeExists("categories"))
        .andExpect(model().attribute("isEdit", false));
  }

  @Test
  @DisplayName("GET /tasks should return tasks view with proper model attributes")
  void showTasks_ShouldReturnTasksView() throws Exception {
    Page<Task> page = new PageImpl<>(List.of());
    when(taskService.getAllTasks(any(TaskFilter.class))).thenReturn(page);
    when(categoryService.findAllCategories()).thenReturn(List.of());

    mockMvc
        .perform(get("/tasks"))
        .andExpect(status().isOk())
        .andExpect(view().name("tasks"))
        .andExpect(model().attributeExists("tasks"))
        .andExpect(model().attributeExists("categories"))
        .andExpect(model().attributeExists("page"))
        .andExpect(model().attributeExists("pageNumbers"));
  }

  @Test
  @DisplayName("GET /tasks with title filter should call searchTasksByTitle")
  void showTasks_titleFilter_ShouldCallSearch() throws Exception {
    TaskFilter filter = new TaskFilter();
    filter.setTitle("ABC");
    Page<Task> page = new PageImpl<>(List.of());
    when(taskService.searchTasksByTitle(any(TaskFilter.class))).thenReturn(page);
    when(categoryService.findAllCategories()).thenReturn(List.of());

    mockMvc
        .perform(get("/tasks").param("title", "ABC"))
        .andExpect(status().isOk())
        .andExpect(view().name("tasks"));

    verify(taskService).searchTasksByTitle(any(TaskFilter.class));
  }

  @Test
  @DisplayName("GET /tasks/edit/{id} should return task edit form")
  void showEditTaskForm_ShouldReturnViewWithPopulatedForm() throws Exception {
    UUID taskId = UUID.randomUUID();
    Task task = new Task();
    task.setId(taskId);
    task.setTitle("Zadanie");
    task.setStatus(Status.TODO);
    task.setDescription("desc");
    task.setDueDate(LocalDateTime.now());
    when(taskService.findTaskById(taskId)).thenReturn(task);
    when(categoryService.findAllCategories()).thenReturn(List.of());

    mockMvc
        .perform(get("/tasks/edit/" + taskId))
        .andExpect(status().isOk())
        .andExpect(view().name("task-add"))
        .andExpect(model().attributeExists("task"))
        .andExpect(model().attributeExists("categories"))
        .andExpect(model().attribute("isEdit", true))
        .andExpect(model().attribute("taskId", taskId));
  }

  @Test
  @DisplayName("POST /tasks/edit/{id} with valid data should update and redirect")
  void updateTask_ShouldUpdateAndRedirect() throws Exception {
    UUID taskId = UUID.randomUUID();
    when(categoryService.findAllCategories()).thenReturn(List.of());
    when(taskService.updateTask(eq(taskId), any(UpdateTaskRequest.class))).thenReturn(new Task());

    mockMvc
        .perform(
            post("/tasks/edit/" + taskId)
                .param("title", "NewTitle")
                .param("status", "TODO")
                .param("description", "desc")
                .param("dueDate", "2024-12-20T10:00")
                .contentType("application/x-www-form-urlencoded"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tasks"));

    verify(taskService).updateTask(eq(taskId), any(UpdateTaskRequest.class));
  }

  @Test
  @DisplayName("POST /tasks/edit/{id} with invalid data should stay on form")
  void updateTask_WithInvalidData_ShouldReturnToEditForm() throws Exception {
    UUID taskId = UUID.randomUUID();
    when(categoryService.findAllCategories()).thenReturn(List.of());

    mockMvc
        .perform(
            post("/tasks/edit/" + taskId)
                .param(
                    "description",
                    "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem.")
                .contentType("application/x-www-form-urlencoded"))
        .andExpect(status().isOk())
        .andExpect(view().name("task-add"))
        .andExpect(model().attributeExists("categories"))
        .andExpect(model().attribute("isEdit", true))
        .andExpect(model().attribute("taskId", taskId));
  }

  @Test
  @DisplayName("GET /tasks/edit/{id} should return form with category when task has category")
  void showEditTaskForm_ShouldIncludeCategoryIdInModel_IfTaskHasCategory() throws Exception {
    UUID taskId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    Task task = new Task();
    task.setId(taskId);
    task.setTitle("Zadanie z kategorią");
    task.setStatus(Status.TODO);
    task.setDescription("desc");
    task.setDueDate(LocalDateTime.now());

    Category category = new Category();
    category.setId(categoryId);
    task.setCategory(category);

    when(taskService.findTaskById(taskId)).thenReturn(task);
    when(categoryService.findAllCategories()).thenReturn(List.of(category));

    mockMvc
        .perform(get("/tasks/edit/" + taskId))
        .andExpect(status().isOk())
        .andExpect(view().name("task-add"))
        .andExpect(model().attributeExists("task"))
        .andExpect(model().attributeExists("categories"))
        .andExpect(model().attribute("isEdit", true))
        .andExpect(model().attribute("taskId", taskId))
        .andExpect(model().attribute("categories", List.of(category)))
        .andExpect(
            model()
                .attribute(
                    "task",
                    org.hamcrest.Matchers.hasProperty(
                        "categoryId", org.hamcrest.Matchers.equalTo(categoryId))));
  }

  @Test
  @DisplayName("GET /tasks/{id}/delete should delete and redirect")
  void deleteTask_ShouldDeleteAndRedirect() throws Exception {
    UUID taskId = UUID.randomUUID();

    mockMvc
        .perform(get("/tasks/" + taskId + "/delete"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tasks"));

    verify(taskService).deleteTaskById(taskId);
  }

  @Test
  @DisplayName("GET /tasks/{id} should show task detail")
  void showTask_ShouldShowTaskDetails() throws Exception {
    UUID taskId = UUID.randomUUID();
    Task task = new Task();
    task.setId(taskId);

    when(taskService.findTaskById(taskId)).thenReturn(task);

    mockMvc
        .perform(get("/tasks/" + taskId))
        .andExpect(status().isOk())
        .andExpect(view().name("task-info"))
        .andExpect(model().attributeExists("task"));
  }
}
