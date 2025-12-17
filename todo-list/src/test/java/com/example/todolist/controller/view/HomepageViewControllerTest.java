package com.example.todolist.controller.view;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.todolist.service.TaskService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomepageController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("HomepageController")
class HomepageViewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TaskService taskService;

  @Test
  @DisplayName("GET / should return index view with stats and upcomingTasks in model")
  void home_ReturnsIndexViewWithModelAttributes() throws Exception {
    Map<String, Long> stats = new HashMap<>();
    stats.put("totalTasks", 5L);
    stats.put("doneTasks", 2L);
    stats.put("todoTasks", 2L);
    stats.put("inProgressTasks", 1L);

    when(taskService.getStats()).thenReturn(stats);
    when(taskService.getUpcomingTasks())
        .thenReturn(new org.springframework.data.domain.PageImpl<>(Collections.emptyList()));

    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"))
        .andExpect(model().attributeExists("stats"))
        .andExpect(model().attributeExists("upcomingTasks"));

    verify(taskService).getStats();
    verify(taskService).getUpcomingTasks();
  }
}
