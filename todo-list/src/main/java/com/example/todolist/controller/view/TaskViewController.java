package com.example.todolist.controller.view;

import com.example.todolist.dto.CreateTaskRequest;
import com.example.todolist.entity.Task;
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

    // GET: formularz dodawania zadania
    @GetMapping("/add")
    public String showAddTaskForm(Model model) {
        model.addAttribute("task", new CreateTaskRequest());
        model.addAttribute("categories", categoryService.findAllCategories());
        return "task-add"; // plik Thymeleaf task-add.html
    }

    // POST: zapis nowego zadania
    @PostMapping("/add")
    public String submitNewTask(
            @Valid @ModelAttribute("task") CreateTaskRequest dto,
            BindingResult bindingResult,
            Model model
    ) {
        // jeśli są błędy walidacji, wracamy do formularza
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllCategories());
            return "task-add";
        }

        // zapis zadania
        taskService.createTask(dto);

        // przekierowanie na listę zadań
        return "redirect:/";
    }
}
