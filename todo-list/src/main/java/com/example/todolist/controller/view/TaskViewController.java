package com.example.todolist.controller.view;

import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.Task;
import com.example.todolist.service.CategoryService;
import com.example.todolist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
            model.addAttribute("isEdit", false);
            return "task-add";
        }

        taskService.createTask(dto);
        return "redirect:/tasks";
    }

    @GetMapping
    public String showTasks(
            @RequestParam(name = "sort", defaultValue = "title") String sort,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "categoryId", required = false) UUID categoryId,
            @RequestParam(name = "dueAfter", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueAfter,
            @RequestParam(name = "dueBefore", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
            Model model
    ) {
        List<Task> tasks = taskService.getAllTasks(sort, direction, status, categoryId, dueAfter, dueBefore);
        List<Category> categories = categoryService.findAllCategories();

        model.addAttribute("tasks", tasks);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDirection", direction.toLowerCase());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("categories", categories);

        model.addAttribute("dueAfter", dueAfter);
        model.addAttribute("dueBefore", dueBefore);

        return "tasks";
    }
    @GetMapping("/tasksByTitle")
    public String showTasksByTitle(@RequestParam(name = "title", required = false) String title, Model model){
        List<Task> tasks = taskService.searchTasksByTitle(title, 0, 50);
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("tasks", tasks);
        model.addAttribute("searchTitle", title);
        model.addAttribute("categories", categories);

        return "tasks";
    }
    @GetMapping("/edit/{id}")
    public String showEditTaskForm(@PathVariable UUID id, Model model) {
        Task task = taskService.findTaskById(id);

        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
        updateRequest.setTitle(task.getTitle());
        updateRequest.setDescription(task.getDescription());
        updateRequest.setStatus(task.getStatus());
        updateRequest.setDueDate(task.getDueDate());
        if (task.getCategory() != null) {
            updateRequest.setCategoryId(task.getCategory().getId());
        }

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

    @GetMapping("/{taskId}")
    public String showTask(@PathVariable UUID taskId, Model model) {
        Task task = taskService.findTaskById(taskId);
        model.addAttribute("task", task);
        return "task-info";
    }
}