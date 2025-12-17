package com.example.todolist.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.todolist.dto.request.RegisterRequest;
import com.example.todolist.entity.User;
import com.example.todolist.exception.UserAlreadyExistsException;
import com.example.todolist.repository.UserRepository;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock UserRepository userRepository;

    @Mock PasswordEncoder encoder;

    @InjectMocks UserService userService;

    @Test
    @DisplayName("registerUser creates new user when email does not exist")
    void registerUser_createsUser_WhenEmailNotExists() {
        RegisterRequest dto = new RegisterRequest("mail@test.com", "pass123");
        when(userRepository.findByEmail("mail@test.com")).thenReturn(Optional.empty());
        when(encoder.encode("pass123")).thenReturn("encodedPassword");

        userService.register(dto);

        verify(userRepository).findByEmail("mail@test.com");
        verify(encoder).encode("pass123");
        verify(userRepository).save(
                argThat(
                        user ->
                                user.getEmail().equals("mail@test.com")
                                        && user.getPassword().equals("encodedPassword")
                                        && user.getRole().equals("USER")));
    }

    @Test
    @DisplayName("registerUser throws exception when email exists")
    void registerUser_throws_WhenUserExists() {
        RegisterRequest dto = new RegisterRequest("mail@test.com", "pass123");
        when(userRepository.findByEmail("mail@test.com")).thenReturn(Optional.of(new User()));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(dto));

        verify(userRepository).findByEmail("mail@test.com");
        verify(userRepository, never()).save(any());
    }
    @Test
    @DisplayName("getCurrentUser returns found user by email from SecurityContext")
    void getCurrentUser_ReturnsUser() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "password")
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser();

        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getCurrentUser throws when user with email not found")
    void getCurrentUser_ThrowsIfNotFound() {
        String email = "missing@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "password")
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.getCurrentUser()
        );
        assertTrue(ex.getMessage().contains("User not found"));
        verify(userRepository).findByEmail(email);
    }
}