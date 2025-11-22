package com.example.todolist.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import com.example.todolist.entity.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Query("""
    SELECT t FROM Task t
    WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Task> searchTasksByTitle(@Param("keyword") String keyword, Pageable pageable);
}
