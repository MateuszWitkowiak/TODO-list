package com.example.todolist.controller.view;


import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.dto.request.UpdateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryViewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CategoryViewController")
class CategoryViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("GET /categories/add should return add category form view")
    void showAddCategoryForm_shouldReturnAddView() throws Exception {
        mockMvc.perform(get("/categories/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("category-add"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attribute("category", instanceOf(CreateCategoryRequest.class)));
    }

    @Test
    @DisplayName("POST /categories should create category and redirect to root")
    void createCategory_ShouldCreateCategoryAndRedirect() throws Exception {
        mockMvc.perform(post("/categories")
                        .param("name", "NewCat")
                        .param("color", "#111111")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        ArgumentCaptor<CreateCategoryRequest> captor = ArgumentCaptor.forClass(CreateCategoryRequest.class);
        verify(categoryService).createCategory(captor.capture());
        assertEquals("NewCat", captor.getValue().getName());
        assertEquals("#111111", captor.getValue().getColor());
    }

    @Test
    @DisplayName("GET /categories should return sorted categories view")
    void showCategories_shouldReturnViewWithSortedCategories() throws Exception {
        Category cat1 = new Category();
        cat1.setName("A");
        Category cat2 = new Category();
        cat2.setName("B");

        when(categoryService.findAllCategories("name", "asc")).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("currentSort", "name"))
                .andExpect(model().attribute("currentDirection", "asc"));

        verify(categoryService).findAllCategories("name", "asc");
    }

    @Test
    @DisplayName("GET /categories/edit/{id} should return edit form view with category")
    void showEditCategoryForm_shouldReturnCategoryEditForm() throws Exception {
        UUID catId = UUID.randomUUID();
        Category cat = new Category();
        cat.setId(catId);
        cat.setName("Existing");

        when(categoryService.findCategoryById(catId)).thenReturn(cat);

        mockMvc.perform(get("/categories/edit/" + catId))
                .andExpect(status().isOk())
                .andExpect(view().name("category-add"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attribute("isEdit", true));

        verify(categoryService).findCategoryById(catId);
    }

    @Test
    @DisplayName("POST /categories/edit/{id} with valid data updates category and redirects")
    void updateTask_shouldUpdateCategoryAndRedirect() throws Exception {
        UUID catId = UUID.randomUUID();
        UpdateCategoryRequest updateRequest = new UpdateCategoryRequest("Edited", "#FFF");

        when(categoryService.updateCategory(eq(catId), any(UpdateCategoryRequest.class)))
                .thenReturn(new Category());

        mockMvc.perform(post("/categories/edit/" + catId)
                        .param("name", "Edited")
                        .param("color", "#FFF")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService).updateCategory(eq(catId), any(UpdateCategoryRequest.class));
    }

    @Test
    @DisplayName("POST /categories/edit/{id} with invalid data should stay on form")
    void updateTask_shouldReturnFormOnValidationFail() throws Exception {
        UUID catId = UUID.randomUUID();

        mockMvc.perform(post("/categories/edit/" + catId)
                        .param("color", "")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().isOk())
                .andExpect(view().name("category-add"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("categoryId", catId));
    }

    @Test
    @DisplayName("GET /categories/{id}/delete should delete category and redirect")
    void deleteTask_shouldDeleteCategoryAndRedirect() throws Exception {
        UUID catId = UUID.randomUUID();

        mockMvc.perform(get("/categories/" + catId + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService).deleteCategoryById(catId);
    }
}