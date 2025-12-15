package com.example.todolist.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.todolist.entity.Category;
import com.example.todolist.entity.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class CategoryRepositoryTest {

  @Autowired private CategoryRepository categoryRepository;
  @Autowired private UserRepository userRepository;

  // ---------- Helpers ------------

  private User createUser(String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("pass");
    user.setRole("USER");
    return userRepository.save(user);
  }

  private void createCategory(String name, User user) {
    Category c = new Category();
    c.setName(name);
    c.setColor("#FFFFFF");
    c.setUser(user);
    categoryRepository.save(c);
  }

  // ------------ Tests ---------------

  @Test
  void findAllByUserId_shouldReturnUserCategories() {
    User user = createUser("a@gmail.com");
    createCategory("Work", user);
    createCategory("Home", user);

    User fakeUser = createUser("fake@gmail.com");
    createCategory("School", fakeUser);
    createCategory("Groceries", fakeUser);
    createCategory("Shopping", fakeUser);

    List<Category> categories = categoryRepository.findAllByUserId(user.getId());

    assertEquals(2, categories.size());
    assertTrue(categories.stream().allMatch(c -> c.getUser().getId().equals(user.getId())));
    assertEquals("Work", categories.get(0).getName());
    assertEquals("Home", categories.get(1).getName());
  }
}
