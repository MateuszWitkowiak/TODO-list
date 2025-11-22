package com.example.todolist.controller.api;

import com.example.todolist.dto.CreateTaskRequest;
import com.example.todolist.dto.UpdateTaskRequest;
import com.example.todolist.entity.Task;
import com.example.todolist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

    @MockBean
    private TaskService taskService;

    @Test
    void getAllTasks_ShouldReturnList() throws Exception {
        Task t1 = new Task();
        t1.setId(UUID.randomUUID());
        t1.setTitle("Test 1");

        Task t2 = new Task();
        t2.setId(UUID.randomUUID());
        t2.setTitle("Test 2");

        when(taskService.getAllTasks()).thenReturn(List.of(t1, t2));

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

        when(taskService.findTaskById(id)).thenReturn(task);

        mockMvc.perform(get("/api/v1/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test")));
    }

    @Test
    void searchTasks_ShouldReturnFilteredList() throws Exception {
        Task t1 = new Task();
        t1.setId(UUID.randomUUID());
        t1.setTitle("Hello world");

        when(taskService.searchTasksByTitle("Hello", 0, 10)).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/tasks/search")
                        .param("keyword", "Hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Hello world")));
    }

    @Test
    void createTask_ShouldReturnCreatedTask() throws Exception {
        UUID id = UUID.randomUUID();

        Task created = new Task();
        created.setId(id);
        created.setTitle("New task");

        when(taskService.createTask(any(CreateTaskRequest.class)))
                .thenReturn(created);

        String json = """
            {
              "title": "New task",
              "description": "some desc",
              "status": "TODO",
              "dueDate": "2025-01-01T10:00:00",
              "categoryId": "7e7c913d-1a4d-402e-a78b-6963bbf87d1d"
            }
            """;

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

        when(taskService.updateTask(any(UUID.class), any(UpdateTaskRequest.class)))
                .thenReturn(updated);

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
