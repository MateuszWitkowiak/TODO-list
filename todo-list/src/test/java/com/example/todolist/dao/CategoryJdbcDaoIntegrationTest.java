package com.example.todolist.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.example.todolist.entity.Category;
import com.example.todolist.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CategoryJdbcDaoIntegrationTest {

  @Autowired private CategoryJdbcDao categoryJdbcDao;

  @Autowired private JdbcTemplate jdbcTemplate;

  private UUID userId;

  private static final String CREATE_USERS_TABLE =
      """
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(36) PRIMARY KEY,
                email VARCHAR(255),
                password VARCHAR(50),
                role VARCHAR(32)
            )
            """;
  private static final String CREATE_CATEGORIES_TABLE =
      """
            CREATE TABLE IF NOT EXISTS categories (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                color VARCHAR(100),
                user_id VARCHAR(36) NOT NULL,
                CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """;

  @BeforeAll
  void setUpSchema() {
    jdbcTemplate.execute(CREATE_USERS_TABLE);
    jdbcTemplate.execute(CREATE_CATEGORIES_TABLE);

    userId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO users (id, email, password, role) VALUES (?, ?, ?, ?)",
        userId.toString(),
        "cat@test.com",
        "passcat",
        "USER");
  }

  @BeforeEach
  void setUp() {
    jdbcTemplate.execute("DELETE FROM tasks");
    jdbcTemplate.execute("DELETE FROM categories");
  }

  @Test
  @DisplayName("insertAndFindById: should persist and fetch category by id and user")
  void insertAndFindById_ShouldPersistAndFetchCategory() {
    UUID uuid = UUID.randomUUID();
    Category c = new Category();
    c.setId(uuid);
    c.setName("Home");
    c.setColor("Red");
    User user = new User();
    user.setId(userId);
    c.setUser(user);

    int affectedRows = categoryJdbcDao.insert(c);
    assertEquals(1, affectedRows);

    Category fetched = categoryJdbcDao.findById(uuid);
    assertEquals(uuid, fetched.getId());
    assertEquals("Home", fetched.getName());
    assertEquals("Red", fetched.getColor());
    assertNotNull(fetched.getUser());
    assertEquals(userId, fetched.getUser().getId());
  }

  @Test
  @DisplayName("findAllByUserId: should return all inserted categories for user")
  void findAllByUserId_ShouldReturnCategories() {
    User user = new User();
    user.setId(userId);

    Category c1 = new Category();
    c1.setId(UUID.randomUUID());
    c1.setName("Job");
    c1.setColor("Blue");
    c1.setUser(user);

    Category c2 = new Category();
    c2.setId(UUID.randomUUID());
    c2.setName("Travel");
    c2.setColor("Green");
    c2.setUser(user);

    categoryJdbcDao.insert(c1);
    categoryJdbcDao.insert(c2);

    List<Category> categories = categoryJdbcDao.findAllByUserId(userId);
    assertEquals(2, categories.size());
    assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Job")));
    assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Travel")));
    assertTrue(
        categories.stream()
            .allMatch(c -> c.getUser() != null && c.getUser().getId().equals(userId)));
  }

  @Test
  @DisplayName("update: should modify name and color of category")
  void update_ShouldModifyCategory() {
    User user = new User();
    user.setId(userId);

    UUID uuid = UUID.randomUUID();
    Category c = new Category();
    c.setId(uuid);
    c.setName("Before");
    c.setColor("Yellow");
    c.setUser(user);

    categoryJdbcDao.insert(c);

    c.setName("After");
    c.setColor("Cyan");

    int affected = categoryJdbcDao.update(c);
    assertEquals(1, affected);

    Category updated = categoryJdbcDao.findById(uuid);
    assertEquals("After", updated.getName());
    assertEquals("Cyan", updated.getColor());
    assertNotNull(updated.getUser());
    assertEquals(userId, updated.getUser().getId());
  }

  @Test
  @DisplayName("delete: should delete category and not find it by id")
  void delete_ShouldRemoveCategory() {
    User user = new User();
    user.setId(userId);

    UUID uuid = UUID.randomUUID();
    Category c = new Category();
    c.setId(uuid);
    c.setName("DelCat");
    c.setColor("Black");
    c.setUser(user);

    categoryJdbcDao.insert(c);
    int removed = categoryJdbcDao.deleteById(uuid);
    assertEquals(1, removed);

    assertThrows(Exception.class, () -> categoryJdbcDao.findById(uuid));
  }
}
