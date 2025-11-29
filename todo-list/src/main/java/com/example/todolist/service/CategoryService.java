package com.example.todolist.service;

import com.example.todolist.dto.CreateCategoryRequest;
import com.example.todolist.dto.UpdateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.User;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("id", categoryId));
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException("id", categoryId));
    }

    @Transactional
    public Category createCategory(UUID userId, CreateCategoryRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Category category = new Category();
        category.setName(dto.name());
        category.setColor(dto.color());
        category.setUser(user);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(UUID id, UpdateCategoryRequest update) {
        Category category = findCategoryById(id);

        if (update.name() != null) {
            category.setName(update.name());
        }
        if (update.color() != null) {
            category.setColor(update.color());
        }

        return categoryRepository.save(category);
    }
}
