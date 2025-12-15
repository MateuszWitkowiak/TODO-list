package com.example.todolist.controller.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.todolist.dto.mapper.CategoryMapper;
import com.example.todolist.dto.request.CreateCategoryRequest;
import com.example.todolist.dto.request.UpdateCategoryRequest;
import com.example.todolist.dto.response.CreateCategoryResponse;
import com.example.todolist.dto.response.GetCategoryResponse;
import com.example.todolist.entity.Category;
import com.example.todolist.exception.CategoryNotFoundException;
import com.example.todolist.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryApiControllerTest {

  private static final String BASE_URL = "/api/v1/categories";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CategoryService categoryService;

  @MockitoBean private CategoryMapper categoryMapper;

  private ObjectMapper objectMapper;

  private UUID categoryId;
  private Category categoryWork;
  private Category categoryHome;
  private GetCategoryResponse getResponseWork;
  private GetCategoryResponse getResponseHome;
  private CreateCategoryResponse createResponse;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();

    categoryId = UUID.randomUUID();

    categoryWork = new Category();
    categoryWork.setId(categoryId);
    categoryWork.setName("Work");
    categoryWork.setColor("#FFFFFF");

    categoryHome = new Category();
    categoryHome.setId(UUID.randomUUID());
    categoryHome.setName("Home");
    categoryHome.setColor("#BBBBBB");

    getResponseWork =
        new GetCategoryResponse(
            categoryWork.getId(), categoryWork.getName(), categoryWork.getColor(), null);

    getResponseHome =
        new GetCategoryResponse(
            categoryHome.getId(), categoryHome.getName(), categoryHome.getColor(), null);

    createResponse =
        new CreateCategoryResponse(
            categoryWork.getId(), categoryWork.getName(), categoryWork.getColor(), null);
  }

  @Test
  @DisplayName("GET /api/v1/categories should return list of categories")
  void shouldReturnAllCategories() throws Exception {
    when(categoryService.findAllCategories()).thenReturn(List.of(categoryWork, categoryHome));
    when(categoryMapper.mapToGetCategoryResponse(List.of(categoryWork, categoryHome)))
        .thenReturn(List.of(getResponseWork, getResponseHome));

    mockMvc
        .perform(get(BASE_URL))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(categoryWork.getId().toString())))
        .andExpect(jsonPath("$[0].name", is("Work")))
        .andExpect(jsonPath("$[0].color", is("#FFFFFF")))
        .andExpect(jsonPath("$[1].id", is(categoryHome.getId().toString())))
        .andExpect(jsonPath("$[1].name", is("Home")))
        .andExpect(jsonPath("$[1].color", is("#BBBBBB")));
  }

  @Test
  @DisplayName("GET /api/v1/categories/{id} should return single category")
  void shouldReturnCategoryById() throws Exception {
    when(categoryService.findCategoryById(categoryId)).thenReturn(categoryWork);
    when(categoryMapper.mapToGetCategoryResponse(categoryWork)).thenReturn(getResponseWork);

    mockMvc
        .perform(get(BASE_URL + "/" + categoryId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(categoryId.toString())))
        .andExpect(jsonPath("$.name", is("Work")))
        .andExpect(jsonPath("$.color", is("#FFFFFF")));
  }

  @Test
  @DisplayName("GET /api/v1/categories/{id} should return 404 when category not found")
  void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
    when(categoryService.findCategoryById(categoryId))
        .thenThrow(new CategoryNotFoundException("Not found", categoryId));

    mockMvc.perform(get(BASE_URL + "/" + categoryId)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/v1/categories should create category and return 201")
  void shouldCreateCategory() throws Exception {
    CreateCategoryRequest request = new CreateCategoryRequest("Work", "#FFFFFF");
    String json = objectMapper.writeValueAsString(request);

    when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(categoryWork);
    when(categoryMapper.mapToCreateCategoryResponse(categoryWork)).thenReturn(createResponse);

    mockMvc
        .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(categoryId.toString())))
        .andExpect(jsonPath("$.name", is("Work")))
        .andExpect(jsonPath("$.color", is("#FFFFFF")));
  }

  @Test
  @DisplayName("PUT /api/v1/categories/{id} should update existing category")
  void shouldUpdateCategory() throws Exception {
    UpdateCategoryRequest updateRequest = new UpdateCategoryRequest("School", "#000000");
    String json = objectMapper.writeValueAsString(updateRequest);

    Category updatedCategory = new Category();
    updatedCategory.setId(categoryId);
    updatedCategory.setName("School");
    updatedCategory.setColor("#000000");

    GetCategoryResponse updatedResponse =
        new GetCategoryResponse(categoryId, "School", "#000000", null);

    when(categoryService.updateCategory(any(UUID.class), any(UpdateCategoryRequest.class)))
        .thenReturn(updatedCategory);
    when(categoryMapper.mapToGetCategoryResponse(updatedCategory)).thenReturn(updatedResponse);

    mockMvc
        .perform(
            put(BASE_URL + "/" + categoryId).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(categoryId.toString())))
        .andExpect(jsonPath("$.name", is("School")))
        .andExpect(jsonPath("$.color", is("#000000")));
  }

  @Test
  @DisplayName("PUT /api/v1/categories/{id} should return 404 when category not found")
  void shouldReturnNotFoundWhenUpdatingNonExistingCategory() throws Exception {
    UpdateCategoryRequest updateRequest = new UpdateCategoryRequest("School", "#000000");
    String json = objectMapper.writeValueAsString(updateRequest);

    when(categoryService.updateCategory(any(UUID.class), any(UpdateCategoryRequest.class)))
        .thenThrow(new CategoryNotFoundException("Not found", categoryId));

    mockMvc
        .perform(
            put(BASE_URL + "/" + categoryId).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /api/v1/categories/{id} should delete category and return 204")
  void shouldDeleteCategory() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/" + categoryId)).andExpect(status().isNoContent());

    Mockito.verify(categoryService).deleteCategoryById(categoryId);
  }

  @Test
  @DisplayName("DELETE /api/v1/categories/{id} should return 404 when category not found")
  void shouldReturnNotFoundWhenDeletingNonExistingCategory() throws Exception {
    doThrow(new CategoryNotFoundException("Not found", categoryId))
        .when(categoryService)
        .deleteCategoryById(categoryId);

    mockMvc.perform(delete(BASE_URL + "/" + categoryId)).andExpect(status().isNotFound());
  }
}
