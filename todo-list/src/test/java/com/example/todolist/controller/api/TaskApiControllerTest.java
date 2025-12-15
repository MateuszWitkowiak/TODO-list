package com.example.todolist.controller.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.todolist.dto.mapper.TaskMapper;
import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.dto.response.CreateTaskResponse;
import com.example.todolist.dto.response.GetTaskResponse;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskApiControllerTest {

  private static final String BASE_URL = "/api/v1/tasks";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TaskService taskService;

  @MockitoBean private TaskMapper taskMapper;

  @Autowired private ObjectMapper objectMapper;

  // Wspólne dane testowe
  private UUID taskId1;
  private UUID taskId2;
  private Task task1;
  private Task task2;
  private GetTaskResponse getTaskResponse1;
  private GetTaskResponse getTaskResponse2;

  @BeforeEach
  void setUp() {

    taskId1 = UUID.randomUUID();
    taskId2 = UUID.randomUUID();

    task1 = new Task();
    task1.setId(taskId1);
    task1.setTitle("Test 1");

    task2 = new Task();
    task2.setId(taskId2);
    task2.setTitle("Test 2");

    getTaskResponse1 = new GetTaskResponse(taskId1, "Test 1", null, null, null, null, null);

    getTaskResponse2 = new GetTaskResponse(taskId2, "Test 2", null, null, null, null, null);
  }

  @Test
  @DisplayName("GET /api/v1/tasks should return list of tasks")
  void shouldReturnAllTasks() throws Exception {
    // given
    when(taskService.getAllTasks()).thenReturn(List.of(task1, task2));
    when(taskMapper.mapToGetTaskResponse(List.of(task1, task2)))
        .thenReturn(List.of(getTaskResponse1, getTaskResponse2));

    // when / then
    mockMvc
        .perform(get(BASE_URL))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(taskId1.toString())))
        .andExpect(jsonPath("$[0].title", is("Test 1")))
        .andExpect(jsonPath("$[1].id", is(taskId2.toString())))
        .andExpect(jsonPath("$[1].title", is("Test 2")));
  }

  @Test
  @DisplayName("GET /api/v1/tasks/{id} should return single task")
  void shouldReturnTaskById() throws Exception {
    // given
    when(taskService.findTaskById(taskId1)).thenReturn(task1);
    when(taskMapper.mapToGetTaskResponse(task1)).thenReturn(getTaskResponse1);

    // when / then
    mockMvc
        .perform(get(BASE_URL + "/" + taskId1))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(taskId1.toString())))
        .andExpect(jsonPath("$.title", is("Test 1")));
  }

  @Test
  @DisplayName("GET /api/v1/tasks/search should return filtered tasks by keyword")
  void shouldReturnFilteredTasksOnSearch() throws Exception {
    // given
    Task taskHello = new Task();
    UUID helloId = UUID.randomUUID();
    taskHello.setId(helloId);
    taskHello.setTitle("Hello world");

    GetTaskResponse helloResponse =
        new GetTaskResponse(helloId, "Hello world", null, null, null, null, null);

    when(taskService.searchTasksByTitle("Hello", 0, 10)).thenReturn(List.of(taskHello));
    when(taskMapper.mapToGetTaskResponse(List.of(taskHello))).thenReturn(List.of(helloResponse));

    // when / then
    mockMvc
        .perform(get(BASE_URL + "/search").param("keyword", "Hello"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title", is("Hello world")));
  }

  @Test
  @DisplayName("POST /api/v1/tasks should create task and return 201")
  void shouldCreateTask() throws Exception {
    // given
    UUID categoryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID createdTaskId = UUID.randomUUID();

    CreateTaskRequest createRequest =
        new CreateTaskRequest(
            "New task", "some desc", Status.TODO, LocalDateTime.of(2099, 1, 1, 10, 0), categoryId);

    String json = objectMapper.writeValueAsString(createRequest);

    Task createdTask = new Task();
    createdTask.setId(createdTaskId);
    createdTask.setTitle("New task");

    CreateTaskResponse createResponse =
        new CreateTaskResponse(
            createdTaskId, "New task", null, Status.TODO, null, categoryId, userId);

    when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(createdTask);
    when(taskMapper.mapToCreateTaskResponse(createdTask)).thenReturn(createResponse);

    // jeżeli kontroler używa usera z kontekstu
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null));

    // when / then
    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(createdTaskId.toString())))
        .andExpect(jsonPath("$.title", is("New task")))
        .andExpect(jsonPath("$.status", is("TODO")));
  }

  @Test
  @DisplayName("PATCH /api/v1/tasks/{id} should update task and return 200")
  void shouldUpdateTask() throws Exception {
    // given
    UUID id = UUID.randomUUID();

    UpdateTaskRequest updateRequest = new UpdateTaskRequest("Updated", null, null, null, null);

    String json = objectMapper.writeValueAsString(updateRequest);

    Task updated = new Task();
    updated.setId(id);
    updated.setTitle("Updated");

    GetTaskResponse response = new GetTaskResponse(id, "Updated", null, null, null, null, null);

    when(taskService.updateTask(any(UUID.class), any(UpdateTaskRequest.class))).thenReturn(updated);
    when(taskMapper.mapToGetTaskResponse(updated)).thenReturn(response);

    // when / then
    mockMvc
        .perform(patch(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(id.toString())))
        .andExpect(jsonPath("$.title", is("Updated")));
  }

  @Test
  @DisplayName("DELETE /api/v1/tasks/{id} should delete task and return 204")
  void shouldDeleteTask() throws Exception {
    // given
    UUID id = UUID.randomUUID();

    // when / then
    mockMvc.perform(delete(BASE_URL + "/" + id)).andExpect(status().isNoContent());

    verify(taskService).deleteTaskById(id);
  }
}
