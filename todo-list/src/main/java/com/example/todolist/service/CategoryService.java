package com.example.todolist.service;

import com.example.todolist.dto.CreateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void deleteCategoryById(UUID categoryId) {
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("id", categoryId));
        categoryRepository.delete(category);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public Category findCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException("id", categoryId));
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest dto) {
        Category category = new Category();
        category.setName(dto.name());
        category.setColor(dto.color());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(UUID id, Category update) {
        Category category = findCategoryById(id);

        if (update.getName() != null) {
            category.setName(update.getName());
        }
        if (update.getColor() != null) {
            category.setColor(update.getColor());
        }
        return categoryRepository.save(category);
    }
}
