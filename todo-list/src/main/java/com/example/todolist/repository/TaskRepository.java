package com.example.todolist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import com.example.todolist.entity.Tasks;
public interface TaskRepository extends JpaRepository<Tasks, UUID> {
}
