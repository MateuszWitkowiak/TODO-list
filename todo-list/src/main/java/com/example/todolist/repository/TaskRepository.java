package com.example.todolist.repository;

import com.example.todolist.entity.Status;
import org.springframework.data.domain.Page;
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
    WHERE t.user.id = :userId
      AND LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Task> searchTasksByTitle(@Param("userId") UUID userId,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    Page<Task> findByUserIdAndDueDateIsNotNullOrderByDueDateAsc(UUID userId, Pageable pageable);

    List<Task> findAllByUserId(UUID userId);

    Page<Task> findByUserIdAndTitleContainingIgnoreCase(UUID userId, String title, Pageable pageable);

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, Status status);

    List<Task> findAllByCategoryId(UUID categoryId);

}

