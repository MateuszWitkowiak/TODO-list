package com.example.todolist.controller.view;

import com.example.todolist.dto.request.RegisterRequest;
import com.example.todolist.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/register")
  public String registerForm(Model model) {
    model.addAttribute("registerRequest", new RegisterRequest());
    return "register";
  }

  @PostMapping("/register")
  public String registerSubmit(@ModelAttribute RegisterRequest registerRequest) {
    userService.register(registerRequest);

    return "redirect:/login?registered";
  }

  @GetMapping("/login")
  public String loginForm() {
    return "login";
  }
}
