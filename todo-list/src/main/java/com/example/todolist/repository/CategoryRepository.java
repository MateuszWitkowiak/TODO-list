package com.example.todolist.repository;


import com.example.todolist.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Categories, UUID> {
}
