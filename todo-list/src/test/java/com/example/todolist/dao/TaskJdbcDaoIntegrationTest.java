package com.example.todolist.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.User;
import java.time.LocalDateTime;
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
class TaskJdbcDaoIntegrationTest {

  @Autowired private TaskJdbcDao taskJdbcDao;

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
  private static final String CREATE_TASKS_TABLE =
      """
            CREATE TABLE IF NOT EXISTS tasks (
                id VARCHAR(36) PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description VARCHAR(255),
                due_date TIMESTAMP,
                created_at TIMESTAMP NOT NULL,
                status VARCHAR(32) NOT NULL,
                user_id VARCHAR(36) NOT NULL,
                CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """;

  @BeforeAll
  void setUpSchema() {
    jdbcTemplate.execute(CREATE_USERS_TABLE);
    jdbcTemplate.execute(CREATE_TASKS_TABLE);
    userId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO users (id, email, password, role) VALUES (?, ?, ?, ?)",
        userId.toString(),
        "user@dao.test",
        "dao_pass",
        "USER");
  }

  @BeforeEach
  void setUp() {
    jdbcTemplate.execute("DELETE FROM tasks");
  }

  @Test
  @DisplayName("insertAndFindById: should persist task and fetch task by id for user")
  void insertAndFindById_ShouldPersistAndFetchTask() {
    UUID uuid = UUID.randomUUID();
    LocalDateTime due = LocalDateTime.now().withNano(0);
    LocalDateTime created = LocalDateTime.now().withNano(0);

    User user = new User();
    user.setId(userId);

    Task t = new Task();
    t.setId(uuid);
    t.setTitle("Test Title");
    t.setDescription("Desc 123");
    t.setDueDate(due);
    t.setStatus(Status.TODO);
    t.setCreatedAt(created);
    t.setUser(user);

    int affectedRows = taskJdbcDao.insert(t);
    assertEquals(1, affectedRows);

    Task fetched = taskJdbcDao.findById(uuid);
    assertEquals(uuid, fetched.getId());
    assertEquals("Test Title", fetched.getTitle());
    assertEquals("Desc 123", fetched.getDescription());
    assertEquals(due, fetched.getDueDate());
    assertEquals(Status.TODO, fetched.getStatus());
    assertEquals(created.withNano(0), fetched.getCreatedAt().withNano(0));
    assertNotNull(fetched.getUser());
    assertEquals(userId, fetched.getUser().getId());
  }

  @Test
  @DisplayName("findAllByUserId: should return all inserted tasks for user")
  void findAllByUserId_ShouldReturnTasks() {
    LocalDateTime now = LocalDateTime.now().withNano(0);

    User user = new User();
    user.setId(userId);

    Task t1 = new Task();
    t1.setId(UUID.randomUUID());
    t1.setTitle("First");
    t1.setStatus(Status.TODO);
    t1.setCreatedAt(now);
    t1.setUser(user);

    Task t2 = new Task();
    t2.setId(UUID.randomUUID());
    t2.setTitle("Second");
    t2.setStatus(Status.IN_PROGRESS);
    t2.setCreatedAt(now);
    t2.setUser(user);

    taskJdbcDao.insert(t1);
    taskJdbcDao.insert(t2);

    List<Task> tasks = taskJdbcDao.findAllByUserId(userId);
    assertEquals(2, tasks.size());
    assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("First")));
    assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Second")));
    assertTrue(
        tasks.stream().allMatch(t -> t.getUser() != null && t.getUser().getId().equals(userId)));
  }

  @Test
  @DisplayName("update: should modify all fields of existing task")
  void update_ShouldModifyTask() {
    LocalDateTime now = LocalDateTime.now().withNano(0);

    User user = new User();
    user.setId(userId);

    Task t = new Task();
    UUID uuid = UUID.randomUUID();
    t.setId(uuid);
    t.setTitle("Before");
    t.setDescription("Desc Before");
    t.setStatus(Status.TODO);
    t.setCreatedAt(now);
    t.setUser(user);

    taskJdbcDao.insert(t);

    t.setTitle("After");
    t.setDescription("Desc After");
    t.setDueDate(LocalDateTime.of(2023, 1, 2, 11, 30));
    t.setStatus(Status.DONE);

    int affectedRows = taskJdbcDao.update(t);
    assertEquals(1, affectedRows);

    Task updated = taskJdbcDao.findById(uuid);
    assertEquals("After", updated.getTitle());
    assertEquals("Desc After", updated.getDescription());
    assertEquals(LocalDateTime.of(2023, 1, 2, 11, 30), updated.getDueDate());
    assertEquals(Status.DONE, updated.getStatus());
    assertNotNull(updated.getUser());
    assertEquals(userId, updated.getUser().getId());
  }

  @Test
  @DisplayName("delete: should delete task and not find it by id")
  void delete_ShouldRemoveTask() {
    LocalDateTime now = LocalDateTime.now().withNano(0);

    User user = new User();
    user.setId(userId);

    Task t = new Task();
    UUID uuid = UUID.randomUUID();
    t.setId(uuid);
    t.setTitle("Del");
    t.setStatus(Status.TODO);
    t.setCreatedAt(now);
    t.setUser(user);

    taskJdbcDao.insert(t);

    int removed = taskJdbcDao.deleteById(uuid);
    assertEquals(1, removed);

    assertThrows(Exception.class, () -> taskJdbcDao.findById(uuid));
  }
}
