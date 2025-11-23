package com.example.todolist.controller.view;

import com.example.todolist.dto.CreateTaskRequest;
import com.example.todolist.service.CategoryService;
import com.example.todolist.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomepageController {

    private final TaskService taskService;
    private final CategoryService categoryService;

    public HomepageController(TaskService taskService, CategoryService categoryService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("stats", taskService.getStats());
        model.addAttribute("upcomingTasks", taskService.getUpcomingTasks());

        return "index";
    }
}

