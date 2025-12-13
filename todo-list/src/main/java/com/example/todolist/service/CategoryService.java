package com.example.todolist.service;

import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.dto.request.UpdateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.User;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TaskRepository taskRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository, UserService userService, TaskRepository taskRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void deleteCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("id", categoryId));

        List<Task> tasksWithDeletedCategory = taskRepository.findAllByCategoryId(categoryId);
        tasksWithDeletedCategory.forEach(task -> task.setCategory(null));
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAllByUserId(userService.getCurrentUser().getId());
    }

    @Transactional(readOnly = true)
    public Category findCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException("id", categoryId));
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (categoryRepository.existsCategoriesByNameAndUserId(dto.getName(), userService.getCurrentUser().getId())) {
            throw new IllegalArgumentException("Category already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setColor(dto.getColor());
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
