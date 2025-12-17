package com.example.todolist.repository;

import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

  @Query(
      """
    SELECT t FROM Task t
    WHERE t.user.id = :userId
      AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:status IS NULL OR t.status = :status)
      AND (:categoryId IS NULL OR t.category.id = :categoryId)
      AND (:dueAfter IS NULL OR t.dueDate >= :dueAfter)
      AND (:dueBefore IS NULL OR t.dueDate <= :dueBefore)
    """)
  Page<Task> searchTasksByFilter(
      @Param("userId") UUID userId,
      @Param("keyword") String keyword,
      @Param("status") Status status,
      @Param("categoryId") UUID categoryId,
      @Param("dueAfter") LocalDateTime dueAfter,
      @Param("dueBefore") LocalDateTime dueBefore,
      Pageable pageable);

  Page<Task> findByUserIdAndDueDateIsNotNullOrderByDueDateAsc(UUID userId, Pageable pageable);

  List<Task> findAllByUserId(UUID userId);

  Page<Task> findByUserIdAndTitleContainingIgnoreCase(UUID userId, String title, Pageable pageable);

  long countByUserId(UUID userId);

  long countByUserIdAndStatus(UUID userId, Status status);

  List<Task> findAllByCategoryId(UUID categoryId);
}
