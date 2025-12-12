package com.example.todolist.controller.view;

import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.service.CategoryService;
import com.example.todolist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Controller
@RequestMapping("/tasks")
public class TaskViewController {

    private final TaskService taskService;
    private final CategoryService categoryService;
    private final RestTemplate restTemplate;

    private final String API_URL = "http://localhost:8080/api/v1/tasks";

    public TaskViewController(RestTemplate restTemplate, TaskService taskService, CategoryService categoryService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/add")
    public String showAddTaskForm(Model model) {
        model.addAttribute("task", new CreateTaskRequest());
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("isEdit", false);
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

        return "redirect:/tasks";
    }

    @GetMapping
    public String showTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks";
    }

    @GetMapping("/edit/{id}")
    public String showEditTaskForm(@PathVariable UUID id, Model model) {
        var task = taskService.findTaskById(id);

        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
        updateRequest.setTitle(task.getTitle());
        updateRequest.setDescription(task.getDescription());
        updateRequest.setStatus(task.getStatus());
        updateRequest.setDueDate(task.getDueDate());
        updateRequest.setCategoryId(task.getCategory().getId());

        model.addAttribute("task", updateRequest);
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("isEdit", true);
        model.addAttribute("taskId", id);

        return "task-add";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(
            @PathVariable UUID id,
            @Valid @ModelAttribute("task") UpdateTaskRequest dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllCategories());
            model.addAttribute("isEdit", true);
            model.addAttribute("taskId", id);
            return "task-add";
        }

        taskService.updateTask(id, dto);
        return "redirect:/tasks";
    }

    @GetMapping("/{taskId}/delete")
    public String deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTaskById(taskId);
        return "redirect:/tasks";
    }
}
