package com.example.todolist.controller.view;

import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.dto.request.UpdateCategoryRequest;
import com.example.todolist.service.CategoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryViewController {
  private final CategoryService categoryService;

  public CategoryViewController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping("/add")
  public String showAddCategoryForm(Model model) {
    model.addAttribute("category", new CreateCategoryRequest());
    return "category-add";
  }

  @PostMapping
  public String createCategory(@ModelAttribute @Valid CreateCategoryRequest request) {
    categoryService.createCategory(request);
    return "redirect:/";
  }

  @GetMapping
  public String showCategories(
      Model model,
      @RequestParam(name = "sort", defaultValue = "name") String sort,
      @RequestParam(name = "direction", defaultValue = "asc") String direction) {
    model.addAttribute("categories", categoryService.findAllCategories(sort, direction));
    model.addAttribute("currentSort", sort);
    model.addAttribute("currentDirection", direction.toLowerCase());
    return "categories";
  }

  @GetMapping("/edit/{categoryId}")
  public String showEditCategoryForm(@PathVariable UUID categoryId, Model model) {
    model.addAttribute("category", categoryService.findCategoryById(categoryId));
    model.addAttribute("isEdit", true);
    return "category-add";
  }

  @PostMapping("/edit/{categoryId}")
  public String updateTask(
      @PathVariable UUID categoryId,
      @Valid @ModelAttribute("category") UpdateCategoryRequest dto,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("isEdit", true);
      model.addAttribute("categoryId", categoryId);
      return "category-add";
    }

    categoryService.updateCategory(categoryId, dto);
    return "redirect:/categories";
  }

  @GetMapping("/{categoryId}/delete")
  public String deleteTask(@PathVariable UUID categoryId) {
    categoryService.deleteCategoryById(categoryId);
    return "redirect:/categories";
  }
}
