package com.example.todolist.service;

import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.dto.request.UpdateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.User;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    CategoryService categoryService;

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        Category c1 = new Category();
        c1.setId(UUID.randomUUID());
        c1.setName("Work");

        Category c2 = new Category();
        c2.setId(UUID.randomUUID());
        c2.setName("Home");

        List<Category> categories = List.of(c1, c2);

        when(categoryRepository.findAll()).thenReturn(categories);


        List<Category> result = categoryService.findAllCategories();


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Work", result.get(0).getName());
        assertEquals("Home", result.get(1).getName());

        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        Category c1 = new Category();
        UUID id = UUID.randomUUID();
        c1.setId(id);
        c1.setName("Work");
        when(categoryRepository.findById(any())).thenReturn(Optional.of(c1));
        Category result = categoryService.findCategoryById(id);
        assertNotNull(result);
        assertEquals("Work", result.getName());
        verify(categoryRepository).findById(id);
    }

    @Test
    void getCategoryByCategoryId_ShouldThrowExceptionWhenCategoryNotFound() {

        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.findCategoryById(id));

        verify(categoryRepository).findById(id);
    }

    @Test
    void createCategory_ShouldMapDtoAndSaveCategory() {
        // --- given security context ---
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("test@example.com", null);
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // --- given user ---
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Work");
        request.setColor("#FFFFFF");

        Category savedEntity = new Category();
        savedEntity.setName("Work");
        savedEntity.setColor("#FFFFFF");

        when(categoryRepository.save(any(Category.class))).thenReturn(savedEntity);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);

        Category result = categoryService.createCategory(request);

        verify(categoryRepository).save(captor.capture());
        Category passedToRepo = captor.getValue();

        assertEquals("Work", passedToRepo.getName());
        assertEquals("#FFFFFF", passedToRepo.getColor());

        assertNotNull(result);
        assertEquals("Work", result.getName());
        assertEquals("#FFFFFF", result.getColor());
    }


    @Test
    void updateCategory_ShouldUpdateCategory() {
        UUID id = UUID.randomUUID();

        Category existing = new Category();
        existing.setId(id);
        existing.setName("Work");
        existing.setColor("#FFFFFF");

        UpdateCategoryRequest update = new UpdateCategoryRequest("School", "#FF0000");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);

        Category result = categoryService.updateCategory(id, update);

        verify(categoryRepository).save(captor.capture());

        Category saved = captor.getValue();

        assertEquals("School", saved.getName());
        assertEquals("#FF0000", saved.getColor());

        assertEquals("School", result.getName());
        assertEquals("#FF0000", result.getColor());
    }

    @Test
    void updateCategory_ShouldUpdateOnlyName_WhenColorIsNull() {
        UUID id = UUID.randomUUID();

        Category existing = new Category();
        existing.setId(id);
        existing.setName("Work");
        existing.setColor("#FFFFFF");

        UpdateCategoryRequest update = new UpdateCategoryRequest("School", null);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.updateCategory(id, update);

        assertEquals("School", result.getName());
        assertEquals("#FFFFFF", result.getColor());
        verify(categoryRepository).save(existing);
    }

    @Test
    void updateCategory_ShouldUpdateOnlyColor_WhenNameIsNull() {
        UUID id = UUID.randomUUID();

        Category existing = new Category();
        existing.setId(id);
        existing.setName("Work");
        existing.setColor("#FFFFFF");

        UpdateCategoryRequest update = new UpdateCategoryRequest(null, "#000000");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.updateCategory(id, update);

        assertEquals("Work", result.getName());
        assertEquals("#000000", result.getColor());
        verify(categoryRepository).save(existing);
    }

    @Test
    void updateCategory_ShouldThrowExceptionWhenCategoryNotFound() {
        UUID id = UUID.randomUUID();
        UpdateCategoryRequest update = new UpdateCategoryRequest("New", "#123123");

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.updateCategory(id, update));

        verify(categoryRepository).findById(id);
    }

    @Test
    void deleteCategoryById_ShouldDeleteCategoryById() {
        UUID id = UUID.randomUUID();

        Category category = new Category();
        category.setId(id);
        category.setName("Work");
        category.setColor("#FFFFFF");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        categoryService.deleteCategoryById(id);

        verify(categoryRepository).findById(id);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategoryById_shouldThrowExceptionWhenCategoryNotFound() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategoryById(id));

        verify(categoryRepository).findById(id);
    }
}
