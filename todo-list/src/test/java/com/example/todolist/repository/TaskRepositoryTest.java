package com.example.todolist.repository;

import com.example.todolist.entity.Category;
import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class TaskRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void searchTasksByTitle_shouldReturnMatchingTasks() {
        Category cat = new Category();
        cat.setName("Work");
        cat.setColor("#FFFFFF");
        categoryRepository.save(cat);

        Task t1 = new Task();
        t1.setTitle("Shopping list");
        t1.setStatus(Status.TODO);
        t1.setCategory(cat);
        taskRepository.save(t1);

        Task t2 = new Task();
        t2.setTitle("Shop for car parts");
        t2.setStatus(Status.TODO);
        t2.setCategory(cat);
        taskRepository.save(t2);

        Task t3 = new Task();
        t3.setTitle("Random task");
        t3.setStatus(Status.TODO);
        t3.setCategory(cat);
        taskRepository.save(t3);

        List<Task> results = taskRepository.searchTasksByTitle("shop", PageRequest.of(0, 10));

        // then
        assertEquals(2, results.size());
        assertTrue(results.stream()
                .allMatch(t -> t.getTitle().toLowerCase().contains("shop")));
    }
}
