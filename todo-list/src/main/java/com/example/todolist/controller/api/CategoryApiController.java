package com.example.todolist.controller.api;

import com.example.todolist.dto.CreateCategoryRequest;
import com.example.todolist.dto.UpdateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private final CategoryService categoryService;

    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.findAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(categoryService.findCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody @Valid CreateCategoryRequest dto) {
        Category created = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable("id") UUID id, @RequestBody UpdateCategoryRequest update) {
        Category updated = categoryService.updateCategory(id, update);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}
