package com.example.todolist.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.dto.request.UpdateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.entity.User;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.repository.CategoryRepository;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.service.CategoryService;
import com.example.todolist.service.UserService;
import java.util.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService")
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    @Mock TaskRepository taskRepository;
    @InjectMocks CategoryService categoryService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
    }

    private Category cat(String name) {
        Category c = new Category();
        c.setName(name);
        return c;
    }

    @Nested
    @DisplayName("findCategoryById")
    class FindCategoryById {

        @Test
        @DisplayName("should return category when found")
        void shouldReturnCategory() {
            UUID id = UUID.randomUUID();
            Category c = new Category();
            c.setId(id);
            c.setName("Work");
            when(categoryRepository.findById(id)).thenReturn(Optional.of(c));

            Category result = categoryService.findCategoryById(id);

            assertEquals("Work", result.getName());
            assertEquals(id, result.getId());
        }

        @Test
        @DisplayName("should throw CategoryNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(categoryRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> categoryService.findCategoryById(id));
        }
    }

    @Nested
    @DisplayName("findAllCategories")
    class FindAllCategories {
        @Test
        @DisplayName("Sorts ascending by default (including null direction and case-insensitivity)")
        void sortAscending_DefaultAndNull() {
            when(userService.getCurrentUser()).thenReturn(user);
            List<Category> cats = new ArrayList<>(List.of(cat("Zebra"), cat("ąĄĄ"), cat("abc"), cat("Żaba"), cat("bcd")));
            when(categoryRepository.findAllByUserId(userId)).thenReturn(new ArrayList<>(cats));

            List<Category> sorted1 = categoryService.findAllCategories("name", null);
            List<Category> sorted2 = categoryService.findAllCategories("name", "asc");
            List<Category> sorted3 = categoryService.findAllCategories("name", "ASC");

            List<String> expected = List.of("abc", "ąĄĄ", "bcd", "Zebra", "Żaba");

            for (List<Category> res : List.of(sorted1, sorted2, sorted3)) {
                assertEquals(expected, res.stream().map(Category::getName).toList(), "Should be sorted ascending (polish collation)");
            }
        }

        @Test
        @DisplayName("Sorts descending if direction is desc/DeSc and handles null names")
        void sortDescendingWithNullName() {
            when(userService.getCurrentUser()).thenReturn(user);
            Category c1 = cat("Kot");
            Category c2 = cat("Ądrian");
            Category c3 = cat(null);
            Category c4 = cat("Zebra");
            List<Category> cats = Arrays.asList(c1, c2, c3, c4);

            when(categoryRepository.findAllByUserId(userId))
                    .thenAnswer(inv -> new ArrayList<>(cats));

            List<Category> result = categoryService.findAllCategories("name", "desc");
            List<String> actual = result.stream().map(Category::getName).toList();
            assertEquals(1, actual.stream().filter(Objects::isNull).count(), "Only one null");
            List<String> noNull = actual.stream().filter(Objects::nonNull).toList();
            assertEquals(Arrays.asList("Zebra", "Kot", "Ądrian"), noNull);

            result = categoryService.findAllCategories("name", "DeSc");
            actual = result.stream().map(Category::getName).toList();
            assertEquals(1, actual.stream().filter(Objects::isNull).count(), "Only one null");
            noNull = actual.stream().filter(Objects::nonNull).toList();
            assertEquals(Arrays.asList("Zebra", "Kot", "Ądrian"), noNull);
        }

        @Test
        @DisplayName("Returns empty list if no categories")
        void returnsEmptyList_IfNoCategories() {
            when(userService.getCurrentUser()).thenReturn(user);
            when(categoryRepository.findAllByUserId(userId)).thenReturn(new ArrayList<>());

            List<Category> result = categoryService.findAllCategories("name", "asc");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Handles categories with or without name and mixed diacritics/polish")
        void sortsHandlesDiacriticsAndNulls() {
            when(userService.getCurrentUser()).thenReturn(user);
            Category c1 = cat("żółw");
            Category c2 = cat("Ąbc");
            Category c3 = cat("óbłęd");
            Category c4 = cat(null);
            Category c5 = cat("Zebra");
            Category c6 = cat("aBc");

            List<Category> cats = Arrays.asList(c1,c2,c3,c4,c5,c6);
            when(categoryRepository.findAllByUserId(userId)).thenAnswer(inv -> new ArrayList<>(cats));

            List<Category> resultAsc = categoryService.findAllCategories("name", null);
            List<String> arrAsc = resultAsc.stream().map(Category::getName).toList();

            List<String> expected = Arrays.asList("aBc", "Ąbc", "óbłęd", "Zebra", "żółw", null); // null ostatni
            assertEquals(expected, arrAsc, "Ascending sorts and null last");

            List<Category> resultDesc = categoryService.findAllCategories("name", "desc");
            List<String> arrDesc = resultDesc.stream().map(Category::getName).toList();

            List<String> expectedDesc = new ArrayList<>(expected);
            Collections.reverse(expectedDesc);
            assertEquals(expectedDesc, arrDesc, "Descending sorts and null last");
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("should create category when name is unique")
        void shouldCreateCategory() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("test@example.com", null));

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(userService.getCurrentUser()).thenReturn(user);
            when(categoryRepository.existsCategoriesByNameAndUserId("Work", userId)).thenReturn(false);

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
        @DisplayName("should throw IllegalArgumentException if category name exists for user")
        void shouldThrowIfExists() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("test@example.com", null));

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(userService.getCurrentUser()).thenReturn(user);
            when(categoryRepository.existsCategoriesByNameAndUserId("Work", userId)).thenReturn(true);

            CreateCategoryRequest dto = new CreateCategoryRequest();
            dto.setName("Work");
            dto.setColor("#FFF");

            assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(dto));
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("should update name and color")
        void shouldUpdateBothFields() {
            UUID id = UUID.randomUUID();
            Category existing = new Category();
            existing.setId(id);
            existing.setName("Old");
            existing.setColor("#000");

            UpdateCategoryRequest update = new UpdateCategoryRequest("New", "#FFF");

            when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

            Category result = categoryService.updateCategory(id, update);

            assertEquals("New", result.getName());
            assertEquals("#FFF", result.getColor());
            assertEquals(id, result.getId());
        }

        @Test
        @DisplayName("should throw when category not found")
        void shouldThrow_WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(categoryRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class,
                    () -> categoryService.updateCategory(id, new UpdateCategoryRequest("A", "B")));
        }
    }

    @Nested
    @DisplayName("deleteCategoryById")
    class DeleteCategoryById {

        @Test
        @DisplayName("should delete when category present")
        void shouldDelete() {
            UUID id = UUID.randomUUID();
            Category category = new Category();
            category.setId(id);
            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

            categoryService.deleteCategoryById(id);

            verify(categoryRepository).findById(id);
            verify(categoryRepository).delete(category);
        }

        @Test
        @DisplayName("should throw when category not found")
        void shouldThrow_OnNotFound() {
            UUID id = UUID.randomUUID();
            when(categoryRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategoryById(id));
            verify(categoryRepository, never()).delete(any());
        }
    }
}
