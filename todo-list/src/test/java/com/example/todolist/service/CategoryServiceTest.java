package com.example.todolist.service;

import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.dto.request.UpdateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.User;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @InjectMocks
    CategoryService categoryService;

    @BeforeEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    // ----------------------------------------------------------
    //  findAllCategories()
    // ----------------------------------------------------------
    @Test
    void getAllCategories_ShouldReturnAllCategoriesForUser() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Category c1 = new Category();
        c1.setName("Work");

        Category c2 = new Category();
        c2.setName("Home");

        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(c1, c2));

        List<Category> result = categoryService.findAllCategories();

        assertEquals(2, result.size());
        assertEquals("Work", result.get(0).getName());
        assertEquals("Home", result.get(1).getName());
    }

    // ----------------------------------------------------------
    //  findCategoryById()
    // ----------------------------------------------------------
    @Test
    void getCategoryById_ShouldReturnCategory() {
        UUID id = UUID.randomUUID();
        Category c = new Category();
        c.setId(id);
        c.setName("Work");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(c));

        Category result = categoryService.findCategoryById(id);

        assertEquals("Work", result.getName());
    }

    @Test
    void getCategoryById_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.findCategoryById(id));
    }

    // ----------------------------------------------------------
    //  createCategory()
    // ----------------------------------------------------------
    @Test
    void createCategory_ShouldCreateCategory() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null)
        );

        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.existsCategoriesByNameAndUserId("Work", user.getId()))
                .thenReturn(false);

        CreateCategoryRequest dto = new CreateCategoryRequest();
        dto.setName("Work");
        dto.setColor("#FFF");

        Category saved = new Category();
        saved.setName("Work");
        saved.setColor("#FFF");
        saved.setUser(user);

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        Category result = categoryService.createCategory(dto);

        assertEquals("Work", result.getName());
        assertEquals("#FFF", result.getColor());
        assertEquals(user, result.getUser());
    }

    @Test
    void createCategory_ShouldThrow_WhenExists() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null)
        );

        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.existsCategoriesByNameAndUserId("Work", user.getId()))
                .thenReturn(true);

        CreateCategoryRequest dto = new CreateCategoryRequest();
        dto.setName("Work");
        dto.setColor("#FFF");

        assertThrows(IllegalArgumentException.class,
                () -> categoryService.createCategory(dto));
    }

    // ----------------------------------------------------------
    //  updateCategory()
    // ----------------------------------------------------------
    @Test
    void updateCategory_ShouldUpdateBothFields() {
        UUID id = UUID.randomUUID();

        Category existing = new Category();
        existing.setId(id);
        existing.setName("Old");
        existing.setColor("#000");

        UpdateCategoryRequest update = new UpdateCategoryRequest("New", "#FFF");

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(id, update);

        assertEquals("New", result.getName());
        assertEquals("#FFF", result.getColor());
    }

    @Test
    void updateCategory_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.updateCategory(id, new UpdateCategoryRequest("A", "B")));
    }

    // ----------------------------------------------------------
    //  deleteCategory()
    // ----------------------------------------------------------
    @Test
    void deleteCategory_ShouldDelete() {
        UUID id = UUID.randomUUID();
        Category category = new Category();
        category.setId(id);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        categoryService.deleteCategoryById(id);

        verify(categoryRepository).findById(id);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategoryById(id));
    }
}