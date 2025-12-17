package com.example.todolist.controller.view;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.todolist.dto.request.RegisterRequest;
import com.example.todolist.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController")
class AuthViewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @Test
  @DisplayName("GET /register should return register view with empty form")
  void registerForm_ReturnsRegisterViewWithEmptyForm() throws Exception {
    mockMvc
        .perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"))
        .andExpect(model().attributeExists("registerRequest"))
        .andExpect(model().attribute("registerRequest", instanceOf(RegisterRequest.class)));
  }

  @Test
  @DisplayName("POST /register should register user and redirect to login")
  void registerSubmit_RegistersUser_AndRedirects() throws Exception {
    mockMvc
        .perform(
            post("/register")
                .flashAttr("registerRequest", new RegisterRequest("test@mail.com", "password123")))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?registered"));

    ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
    verify(userService).register(captor.capture());
    RegisterRequest sent = captor.getValue();
    assertEquals("test@mail.com", sent.getEmail());
    assertEquals("password123", sent.getPassword());
  }

  @Test
  @DisplayName("GET /login should return login view")
  void loginForm_ReturnsLoginView() throws Exception {
    mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
  }
}
