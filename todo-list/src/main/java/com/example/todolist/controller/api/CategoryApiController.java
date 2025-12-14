package com.example.todolist.controller.api;

import com.example.todolist.dto.mapper.CategoryMapper;
import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.dto.request.UpdateCategoryRequest;
import com.example.todolist.dto.response.CreateCategoryResponse;
import com.example.todolist.dto.response.GetCategoryResponse;
import com.example.todolist.entity.Category;
import com.example.todolist.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;

  public CategoryApiController(CategoryService categoryService, CategoryMapper categoryMapper) {
    this.categoryService = categoryService;
    this.categoryMapper = categoryMapper;
  }

  @GetMapping
  public ResponseEntity<List<GetCategoryResponse>> getAll() {
    List<Category> categories = categoryService.findAllCategories();
    List<GetCategoryResponse> response = categoryMapper.mapToGetCategoryResponse(categories);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<GetCategoryResponse> getById(@PathVariable("id") UUID id) {
    Category category = categoryService.findCategoryById(id);
    GetCategoryResponse response = categoryMapper.mapToGetCategoryResponse(category);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<CreateCategoryResponse> create(
      @RequestBody @Valid CreateCategoryRequest dto) {
    Category created = categoryService.createCategory(dto);
    CreateCategoryResponse response = categoryMapper.mapToCreateCategoryResponse(created);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<GetCategoryResponse> update(
      @PathVariable("id") UUID id, @RequestBody UpdateCategoryRequest update) {
    Category updated = categoryService.updateCategory(id, update);
    GetCategoryResponse response = categoryMapper.mapToGetCategoryResponse(updated);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    categoryService.deleteCategoryById(id);
    return ResponseEntity.noContent().build();
  }
}
