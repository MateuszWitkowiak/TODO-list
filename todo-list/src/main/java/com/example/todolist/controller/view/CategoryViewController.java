package com.example.todolist.controller.view;

import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String createCategory(@ModelAttribute CreateCategoryRequest request) {
        categoryService.createCategory(request);
        return "redirect:/";
    }
}
