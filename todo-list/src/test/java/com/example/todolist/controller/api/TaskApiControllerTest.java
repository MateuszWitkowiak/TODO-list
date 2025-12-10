package com.example.todolist.controller.api;

import com.example.todolist.dto.mapper.TaskMapper;
import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.dto.response.CreateTaskResponse;
import com.example.todolist.dto.response.GetTaskResponse;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    @Test
    void getAllTasks_ShouldReturnList() throws Exception {
        Task t1 = new Task();
        t1.setId(UUID.randomUUID());
        t1.setTitle("Test 1");

        Task t2 = new Task();
        t2.setId(UUID.randomUUID());
        t2.setTitle("Test 2");

        GetTaskResponse r1 = new GetTaskResponse(t1.getId(), "Test 1", null, null, null, null, null);
        GetTaskResponse r2 = new GetTaskResponse(t2.getId(), "Test 2", null, null, null, null, null);

        when(taskService.getAllTasks()).thenReturn(List.of(t1, t2));
        when(taskMapper.mapToGetTaskResponse(List.of(t1, t2))).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Test 1")))
                .andExpect(jsonPath("$[1].title", is("Test 2")));
    }

    @Test
    void getTaskById_ShouldReturnTask() throws Exception {
        UUID id = UUID.randomUUID();

        Task task = new Task();
        task.setId(id);
        task.setTitle("Test");

        GetTaskResponse response =
                new GetTaskResponse(id, "Test", null, null, null, null, null);

        when(taskService.findTaskById(id)).thenReturn(task);
        when(taskMapper.mapToGetTaskResponse(task)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test")));
    }

    @Test
    void searchTasks_ShouldReturnFilteredList() throws Exception {
        Task t1 = new Task();
        t1.setId(UUID.randomUUID());
        t1.setTitle("Hello world");

        GetTaskResponse r1 = new GetTaskResponse(t1.getId(), "Hello world", null, null, null, null, null);

        when(taskService.searchTasksByTitle("Hello", 0, 10)).thenReturn(List.of(t1));
        when(taskMapper.mapToGetTaskResponse(List.of(t1))).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/v1/tasks/search")
                        .param("keyword", "Hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Hello world")));
    }

    @Test
    void createTask_ShouldReturnCreatedTask() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Task createdTask = new Task();
        createdTask.setId(taskId);
        createdTask.setTitle("New task");

        CreateTaskResponse response =
                new CreateTaskResponse(taskId, "New task", null, Status.TODO, null, categoryId, userId);

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(createdTask);
        when(taskMapper.mapToCreateTaskResponse(createdTask)).thenReturn(response);

        String json = """
        {
          "title": "New task",
          "description": "some desc",
          "status": "TODO",
          "dueDate": "2099-01-01T10:00:00",
          "categoryId": "%s"
        }
        """.formatted(categoryId);

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New task")));
    }


    @Test
    void updateTask_ShouldReturnUpdatedTask() throws Exception {
        UUID id = UUID.randomUUID();

        Task updated = new Task();
        updated.setId(id);
        updated.setTitle("Updated");

        GetTaskResponse response =
                new GetTaskResponse(id, "Updated", null, null, null, null, null);

        when(taskService.updateTask(any(UUID.class), any(UpdateTaskRequest.class)))
                .thenReturn(updated);
        when(taskMapper.mapToGetTaskResponse(updated)).thenReturn(response);

        String json = """
                {"title":"Updated"}
                """;

        mockMvc.perform(patch("/api/v1/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated")));
    }

    @Test
    void deleteTask_ShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tasks/" + id))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTaskById(id);
    }
}
