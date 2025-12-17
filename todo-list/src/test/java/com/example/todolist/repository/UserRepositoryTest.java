package com.example.todolist.repository;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;

import com.example.todolist.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("UserRepository tests")
class UserRepositoryTest {

  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("findByEmail should find user by email")
  void findByEmailFindsUserByEmail() {
    User user = new User();
    user.setEmail("essa@gmail.com");
    user.setPassword("password");
    user.setRole("USER");
    userRepository.save(user);

    Optional<User> foundUser = userRepository.findByEmail("essa@gmail.com");

    assertTrue(foundUser.isPresent(), "User not found");
  }
}
