package com.example.todolist.controller.view;

import com.example.todolist.dto.CreateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/category")
public class CategoryViewController {
    private final CategoryService categoryService;

    public CategoryViewController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public String createCategory(@RequestBody CreateCategoryRequest request) {
        Category newCategory = categoryService.createCategory(request);
        return "redirect:/category/list";
    }
}
