package com.example.todolist.repository;

import com.example.todolist.entity.Category;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  List<Category> findAllByUserId(UUID userId);

  boolean existsCategoriesByNameAndUserId(String name, UUID userId);
}
