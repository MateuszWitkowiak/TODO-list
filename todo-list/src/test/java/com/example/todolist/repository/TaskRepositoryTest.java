package com.example.todolist.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.todolist.entity.Category;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class TaskRepositoryTest {

  @Autowired TaskRepository taskRepository;

  @Autowired CategoryRepository categoryRepository;

  @Autowired UserRepository userRepository;

  // ---------- Helpers ------------

  private User createUser(String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("pass");
    user.setRole("USER");
    return userRepository.save(user);
  }

  private Category createCategory(String name, User user) {
    Category c = new Category();
    c.setName(name);
    c.setColor("#FFFFFF");
    c.setUser(user);
    return categoryRepository.save(c);
  }

  private Task createTask(String title, User user, Category category) {
    Task t = new Task();
    t.setTitle(title);
    t.setStatus(Status.TODO);
    t.setUser(user);
    t.setCategory(category);
    return taskRepository.save(t);
  }

  // ---------- Tests ------------

  @Test
  void searchTasksByTitle_shouldReturnMatchingTasks() {
    User user = createUser("test@example.com");
    Category cat = createCategory("Work", user);

    createTask("Shopping list", user, cat);
    createTask("Shop for car parts", user, cat);
    createTask("Random task", user, cat);

    Page<Task> result =
        taskRepository.searchTasksByTitle(user.getId(), "shop", PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
    assertTrue(
        result.getContent().stream().allMatch(t -> t.getTitle().toLowerCase().contains("shop")));
  }

  @Test
  void findAllByUserId_ShouldReturnTasksForUser() {
    User user1 = createUser("user1@example.com");
    User user2 = createUser("user2@example.com");

    Category cat1 = createCategory("Work", user1);
    Category cat2 = createCategory("Home", user2);

    createTask("Task A", user1, cat1);
    createTask("Task B", user1, cat1);
    createTask("Task C", user2, cat2);

    List<Task> tasks = taskRepository.findAllByUserId(user1.getId());

    assertEquals(2, tasks.size());
    assertTrue(tasks.stream().allMatch(t -> t.getUser().getId().equals(user1.getId())));
  }

  @Test
  void findByUserIdAndDueDateIsNotNullOrderByDueDateAsc_ShouldReturnSortedResults() {
    User user = createUser("sort@example.com");
    Category cat = createCategory("Work", user);

    Task t1 = createTask("A", user, cat);
    t1.setDueDate(LocalDateTime.now().plusDays(5));
    taskRepository.save(t1);

    Task t2 = createTask("B", user, cat);
    t2.setDueDate(LocalDateTime.now().plusDays(1));
    taskRepository.save(t2);

    Task t3 = createTask("C", user, cat);
    t3.setDueDate(null);
    taskRepository.save(t3);

    Page<Task> result =
        taskRepository.findByUserIdAndDueDateIsNotNullOrderByDueDateAsc(
            user.getId(), PageRequest.of(0, 5));

    assertEquals(2, result.getTotalElements());
    assertEquals("B", result.getContent().get(0).getTitle());
    assertEquals("A", result.getContent().get(1).getTitle());
  }

  @Test
  void findByUserIdAndTitleContainingIgnoreCase_ShouldReturnFilteredResults() {
    User user = createUser("filter@example.com");
    Category cat = createCategory("Work", user);

    createTask("Buy Milk", user, cat);
    createTask("milk chocolate", user, cat);
    createTask("Other task", user, cat);

    Page<Task> result =
        taskRepository.findByUserIdAndTitleContainingIgnoreCase(
            user.getId(), "milk", PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
    assertTrue(
        result.getContent().stream().allMatch(t -> t.getTitle().toLowerCase().contains("milk")));
  }

  @Test
  void countByUserId_ShouldReturnCorrectCount() {
    User user1 = createUser("count1@example.com");
    User user2 = createUser("count2@example.com");
    Category c1 = createCategory("Work", user1);
    Category c2 = createCategory("Home", user2);

    createTask("A", user1, c1);
    createTask("B", user1, c1);
    createTask("C", user2, c2);

    long count = taskRepository.countByUserId(user1.getId());

    assertEquals(2, count);
  }

  @Test
  void countByUserIdAndStatus_ShouldReturnCorrectCount() {
    User user = createUser("status@example.com");
    Category cat = createCategory("Work", user);

    Task t1 = createTask("A", user, cat);
    t1.setStatus(Status.TODO);
    taskRepository.save(t1);

    Task t2 = createTask("B", user, cat);
    t2.setStatus(Status.TODO);
    taskRepository.save(t2);

    Task t3 = createTask("C", user, cat);
    t3.setStatus(Status.DONE);
    taskRepository.save(t3);

    long countTodo = taskRepository.countByUserIdAndStatus(user.getId(), Status.TODO);
    long countDone = taskRepository.countByUserIdAndStatus(user.getId(), Status.DONE);

    assertEquals(2, countTodo);
    assertEquals(1, countDone);
  }
}
