package com.example.todolist.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.todolist.entity.Category;
import com.example.todolist.entity.User;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("CategoryRepository tests")
class CategoryRepositoryTest {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    private User user;
    private User fakeUser;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("a@gmail.com");
        user.setPassword("pass");
        user.setRole("USER");
        user = userRepository.save(user);

        fakeUser = new User();
        fakeUser.setEmail("fake@gmail.com");
        fakeUser.setPassword("pass");
        fakeUser.setRole("USER");
        fakeUser = userRepository.save(fakeUser);

        Category c1 = new Category();
        c1.setName("Work");
        c1.setColor("#FFFFFF");
        c1.setUser(user);
        categoryRepository.save(c1);

        Category c2 = new Category();
        c2.setName("Home");
        c2.setColor("#FFFFFF");
        c2.setUser(user);
        categoryRepository.save(c2);

        Category c3 = new Category();
        c3.setName("School");
        c3.setColor("#FFFFFF");
        c3.setUser(fakeUser);
        categoryRepository.save(c3);

        Category c4 = new Category();
        c4.setName("Groceries");
        c4.setColor("#FFFFFF");
        c4.setUser(fakeUser);
        categoryRepository.save(c4);

        Category c5 = new Category();
        c5.setName("Shopping");
        c5.setColor("#FFFFFF");
        c5.setUser(fakeUser);
        categoryRepository.save(c5);
    }

    @Test
    @DisplayName("findAllByUserId should return only categories for given user")
    void findAllByUserId_shouldReturnUserCategories() {
        List<Category> categories = categoryRepository.findAllByUserId(user.getId());

        assertEquals(2, categories.size());
        assertTrue(categories.stream().allMatch(c -> c.getUser().getId().equals(user.getId())));
        assertEquals("Work", categories.get(0).getName());
        assertEquals("Home", categories.get(1).getName());
    }
}