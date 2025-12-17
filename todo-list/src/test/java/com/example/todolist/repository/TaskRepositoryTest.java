package com.example.todolist.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.todolist.entity.Category;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@DisplayName("TaskRepository tests")
class TaskRepositoryTest {

  @Autowired TaskRepository taskRepository;
  @Autowired CategoryRepository categoryRepository;
  @Autowired UserRepository userRepository;

  private User user1;
  private User user2;
  private Category cat1;
  private Category cat2;

  @BeforeEach
  void setUp() {
    user1 = new User();
    user1.setEmail("user1@example.com");
    user1.setPassword("pass");
    user1.setRole("USER");
    user1 = userRepository.save(user1);

    user2 = new User();
    user2.setEmail("user2@example.com");
    user2.setPassword("pass");
    user2.setRole("USER");
    user2 = userRepository.save(user2);

    cat1 = new Category();
    cat1.setName("Work");
    cat1.setColor("#FFFFFF");
    cat1.setUser(user1);
    cat1 = categoryRepository.save(cat1);

    cat2 = new Category();
    cat2.setName("Home");
    cat2.setColor("#FFFFFF");
    cat2.setUser(user2);
    cat2 = categoryRepository.save(cat2);
  }

  @Test
  @DisplayName("searchTasksByFilter should return tasks matching keyword")
  void searchTasksByTitle_shouldReturnMatchingTasks() {
    Task t1 = new Task();
    t1.setTitle("Shopping list");
    t1.setStatus(Status.TODO);
    t1.setUser(user1);
    t1.setCategory(cat1);
    taskRepository.save(t1);

    Task t2 = new Task();
    t2.setTitle("Shop for car parts");
    t2.setStatus(Status.TODO);
    t2.setUser(user1);
    t2.setCategory(cat1);
    taskRepository.save(t2);

    Task t3 = new Task();
    t3.setTitle("Random task");
    t3.setStatus(Status.TODO);
    t3.setUser(user1);
    t3.setCategory(cat1);
    taskRepository.save(t3);

    Page<Task> result =
        taskRepository.searchTasksByFilter(
            user1.getId(), "shop", null, null, null, null, PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
    assertTrue(
        result.getContent().stream().allMatch(t -> t.getTitle().toLowerCase().contains("shop")));
  }

  @Test
  @DisplayName("findAllByUserId should return tasks for user")
  void findAllByUserId_ShouldReturnTasksForUser() {
    Task t1 = new Task();
    t1.setTitle("Task A");
    t1.setStatus(Status.TODO);
    t1.setUser(user1);
    t1.setCategory(cat1);
    taskRepository.save(t1);

    Task t2 = new Task();
    t2.setTitle("Task B");
    t2.setStatus(Status.TODO);
    t2.setUser(user1);
    t2.setCategory(cat1);
    taskRepository.save(t2);

    Task t3 = new Task();
    t3.setTitle("Task C");
    t3.setStatus(Status.TODO);
    t3.setUser(user2);
    t3.setCategory(cat2);
    taskRepository.save(t3);

    List<Task> tasks = taskRepository.findAllByUserId(user1.getId());

    assertEquals(2, tasks.size());
    assertTrue(tasks.stream().allMatch(t -> t.getUser().getId().equals(user1.getId())));
  }

  @Test
  @DisplayName(
      "findByUserIdAndDueDateIsNotNullOrderByDueDateAsc should return tasks sorted by due date")
  void findByUserIdAndDueDateIsNotNullOrderByDueDateAsc_ShouldReturnSortedResults() {
    Task t1 = new Task();
    t1.setTitle("A");
    t1.setStatus(Status.TODO);
    t1.setUser(user1);
    t1.setCategory(cat1);
    t1.setDueDate(LocalDateTime.now().plusDays(5));
    taskRepository.save(t1);

    Task t2 = new Task();
    t2.setTitle("B");
    t2.setStatus(Status.TODO);
    t2.setUser(user1);
    t2.setCategory(cat1);
    t2.setDueDate(LocalDateTime.now().plusDays(1));
    taskRepository.save(t2);

    Task t3 = new Task();
    t3.setTitle("C");
    t3.setStatus(Status.TODO);
    t3.setUser(user1);
    t3.setCategory(cat1);
    t3.setDueDate(null);
    taskRepository.save(t3);

    Page<Task> result =
        taskRepository.findByUserIdAndDueDateIsNotNullOrderByDueDateAsc(
            user1.getId(), PageRequest.of(0, 5));

    assertEquals(2, result.getTotalElements());
    assertEquals("B", result.getContent().get(0).getTitle());
    assertEquals("A", result.getContent().get(1).getTitle());
  }

  @Test
  @DisplayName("countByUserId should return correct task count for user")
  void countByUserId_ShouldReturnCorrectCount() {
    Task t1 = new Task();
    t1.setTitle("A");
    t1.setStatus(Status.TODO);
    t1.setUser(user1);
    t1.setCategory(cat1);
    taskRepository.save(t1);

    Task t2 = new Task();
    t2.setTitle("B");
    t2.setStatus(Status.TODO);
    t2.setUser(user1);
    t2.setCategory(cat1);
    taskRepository.save(t2);

    Task t3 = new Task();
    t3.setTitle("C");
    t3.setStatus(Status.TODO);
    t3.setUser(user2);
    t3.setCategory(cat2);
    taskRepository.save(t3);

    long count = taskRepository.countByUserId(user1.getId());

    assertEquals(2, count);
  }

  @Test
  @DisplayName("countByUserIdAndStatus should return correct count for status")
  void countByUserIdAndStatus_ShouldReturnCorrectCount() {
    Task t1 = new Task();
    t1.setTitle("A");
    t1.setStatus(Status.TODO);
    t1.setUser(user1);
    t1.setCategory(cat1);
    taskRepository.save(t1);

    Task t2 = new Task();
    t2.setTitle("B");
    t2.setStatus(Status.TODO);
    t2.setUser(user1);
    t2.setCategory(cat1);
    taskRepository.save(t2);

    Task t3 = new Task();
    t3.setTitle("C");
    t3.setStatus(Status.DONE);
    t3.setUser(user1);
    t3.setCategory(cat1);
    taskRepository.save(t3);

    long countTodo = taskRepository.countByUserIdAndStatus(user1.getId(), Status.TODO);
    long countDone = taskRepository.countByUserIdAndStatus(user1.getId(), Status.DONE);

    assertEquals(2, countTodo);
    assertEquals(1, countDone);
  }
}
