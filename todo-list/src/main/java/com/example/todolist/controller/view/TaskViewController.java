package com.example.todolist.controller.view;

import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.service.CategoryService;
import com.example.todolist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
public class TaskViewController {

    private final TaskService taskService;
    private final CategoryService categoryService;

    public TaskViewController(TaskService taskService, CategoryService categoryService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
    }

    @GetMapping("/add")
    public String showAddTaskForm(Model model) {
        model.addAttribute("task", new CreateTaskRequest());
        model.addAttribute("categories", categoryService.findAllCategories());
        return "task-add";
    }

    @PostMapping
    public String submitNewTask(
            @Valid @ModelAttribute("task") CreateTaskRequest dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllCategories());
            return "task-add";
        }

        taskService.createTask(dto);

        return "redirect:/";
    }
}
