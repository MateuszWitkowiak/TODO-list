package com.example.todolist.service;

import com.example.todolist.entity.User;
import com.example.todolist.dto.request.RegisterRequest;
import com.example.todolist.exception.UserAlreadyExistsException;
import com.example.todolist.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    UserService userService;

    @Test
    void    registerUser_createsUser_WhenEmailNotExists() {
        RegisterRequest dto = new RegisterRequest("mail@test.com", "pass123");

        when(userRepository.findByEmail("mail@test.com"))
                .thenReturn(Optional.empty());

        when(encoder.encode("pass123"))
                .thenReturn("encodedPassword");

        userService.register(dto);

        verify(userRepository).findByEmail("mail@test.com");
        verify(encoder).encode("pass123");

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("mail@test.com") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRole().equals("USER")
        ));
    }

    @Test
    void registerUser_throws_WhenUserExists() {
        RegisterRequest dto = new RegisterRequest("mail@test.com", "pass123");

        when(userRepository.findByEmail("mail@test.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(dto);
        });

        verify(userRepository).findByEmail("mail@test.com");
        verify(userRepository, never()).save(any());
    }
}
