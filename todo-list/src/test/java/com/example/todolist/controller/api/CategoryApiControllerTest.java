package com.example.todolist.controller.api;

import com.example.todolist.dto.CreateCategoryRequest;
import com.example.todolist.dto.UpdateCategoryRequest;
import com.example.todolist.entity.Category;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getAll_ShouldReturnListOfCategories() throws Exception {
        Category c1 = new Category();
        c1.setId(UUID.randomUUID());
        c1.setName("Work");

        Category c2 = new Category();
        c2.setId(UUID.randomUUID());
        c2.setName("Home");

        when(categoryService.findAllCategories()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Work")))
                .andExpect(jsonPath("$[1].name", is("Home")));
    }

    @Test
    void getById_ShouldReturnCategory() throws Exception {
        UUID id = UUID.randomUUID();

        Category category = new Category();
        category.setId(id);
        category.setName("Work");

        when(categoryService.findCategoryById(id)).thenReturn(category);

        mockMvc.perform(get("/api/v1/categories/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Work")));
    }

    @Test
    void getById_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(categoryService.findCategoryById(id))
                .thenThrow(new CategoryNotFoundException("Not found", id));

        mockMvc.perform(get("/api/v1/categories/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldReturnCreatedCategory() throws Exception {
        UUID id = UUID.randomUUID();

        Category created = new Category();
        created.setId(id);
        created.setName("Work");
        created.setColor("#FFFFFF");

        when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                .thenReturn(created);

        String json = """
                {"name":"Work","color":"#FFFFFF"}
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Work")))
                .andExpect(jsonPath("$.color", is("#FFFFFF")));
    }

    @Test
    void update_ShouldReturnUpdatedCategory() throws Exception {
        UUID id = UUID.randomUUID();

        Category updated = new Category();
        updated.setId(id);
        updated.setName("School");
        updated.setColor("#000000");

        when(categoryService.updateCategory(any(UUID.class), any(UpdateCategoryRequest.class)))
                .thenReturn(updated);

        String json = """
                {"name":"School","color":"#000000"}
                """;

        mockMvc.perform(put("/api/v1/categories/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("School")))
                .andExpect(jsonPath("$.color", is("#000000")));
    }

    @Test
    void update_ShouldReturn404_WhenCategoryNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(categoryService.updateCategory(any(UUID.class), any(UpdateCategoryRequest.class)))
                .thenThrow(new CategoryNotFoundException("Not found", id));

        String json = """
                {"name":"School","color":"#000000"}
                """;

        mockMvc.perform(put("/api/v1/categories/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ShouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/categories/" + id))
                .andExpect(status().isNoContent());

        Mockito.verify(categoryService).deleteCategoryById(id);
    }

    @Test
    void delete_ShouldReturn404_WhenCategoryNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.doThrow(new CategoryNotFoundException("Not found", id))
                .when(categoryService).deleteCategoryById(id);

        mockMvc.perform(delete("/api/v1/categories/" + id))
                .andExpect(status().isNotFound());
    }
}
