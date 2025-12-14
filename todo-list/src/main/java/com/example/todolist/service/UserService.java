package com.example.todolist.service;

import com.example.todolist.dto.request.RegisterRequest;
import com.example.todolist.entity.User;
import com.example.todolist.exception.UserAlreadyExistsException;
import com.example.todolist.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder encoder;

  public UserService(UserRepository userRepository, PasswordEncoder encoder) {
    this.userRepository = userRepository;
    this.encoder = encoder;
  }

  @Transactional
  public void register(RegisterRequest registerDTO) {
    userRepository
        .findByEmail(registerDTO.getEmail())
        .ifPresent(
            u -> {
              throw new UserAlreadyExistsException("User with this email already exists.");
            });

    User user = new User();
    user.setEmail(registerDTO.getEmail());
    user.setPassword(encoder.encode(registerDTO.getPassword()));
    user.setRole("USER");

    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }
}
