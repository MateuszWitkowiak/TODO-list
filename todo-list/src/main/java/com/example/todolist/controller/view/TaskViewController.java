package com.example.todolist.controller.view;

import com.example.todolist.dto.request.CreateTaskRequest;
import com.example.todolist.dto.request.UpdateTaskRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.Task;
import com.example.todolist.service.CategoryService;
import com.example.todolist.service.TaskService;
import com.example.todolist.service.filter.TaskFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("categories", categoryService.findAllCategories());
      model.addAttribute("isEdit", false);
      return "task-add";
    }

    taskService.createTask(dto);
    return "redirect:/tasks";
  }

  @GetMapping
  public String showTasks(@ModelAttribute TaskFilter taskFilter, Model model) {
    Page<Task> taskPage;

    if (taskFilter.getTitle() != null && !taskFilter.getTitle().isBlank()) {
      taskPage = taskService.searchTasksByTitle(taskFilter);
    } else {
      taskPage = taskService.getAllTasks(taskFilter);
    }

    List<Category> categories = categoryService.findAllCategories();

    model.addAttribute("tasks", taskPage.getContent());
    model.addAttribute("page", taskPage);
    model.addAttribute(
        "pageNumbers",
        IntStream.range(0, taskPage.getTotalPages()).boxed().collect(Collectors.toList()));
    model.addAttribute("currentPage", taskPage.getNumber());
    model.addAttribute("pageSize", taskPage.getSize());

    model.addAttribute("currentSort", taskFilter.getSort());
    model.addAttribute(
        "currentDirection",
        (taskFilter.getDirection() != null ? taskFilter.getDirection().toLowerCase() : ""));
    model.addAttribute("selectedStatus", taskFilter.getStatus());
    model.addAttribute("selectedCategory", taskFilter.getCategoryId());
    model.addAttribute("categories", categories);
    model.addAttribute("dueAfter", taskFilter.getDueAfter());
    model.addAttribute("dueBefore", taskFilter.getDueBefore());
    model.addAttribute("searchTitle", taskFilter.getTitle());

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
      Model model) {
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

  @GetMapping("/export")
  public void exportTasks(HttpServletResponse response) {
    taskService.exportTasksToCSV(response);
  }

  @PostMapping("/import")
  public String importTasks(
      @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    try {
      taskService.importTasksFromCsv(file);
      redirectAttributes.addFlashAttribute("successMessage", "Import successful!");
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("errorMessage", "Import error: " + ex.getMessage());
    }
    return "redirect:/tasks";
  }
}
