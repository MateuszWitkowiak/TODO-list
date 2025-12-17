package com.example.todolist.dao;

import com.example.todolist.entity.Status;
import com.example.todolist.entity.Task;
import com.example.todolist.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class TaskJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public TaskJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Task> taskRowMapper = (rs, rowNum) -> {
        Task task = new Task();
        task.setId(UUID.fromString(rs.getString("id")));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setDueDate(rs.getObject("due_date", LocalDateTime.class));
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            task.setStatus(Status.valueOf(statusStr));
        }
        task.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        if (hasColumn(rs, "updated_at")) {
            task.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        }
        if (hasColumn(rs, "user_id")) {
            String userIdStr = rs.getString("user_id");
            if (userIdStr != null) {
                User user = new User();
                user.setId(UUID.fromString(userIdStr));
                task.setUser(user);
            }
        }
        return task;
    };

    private static boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Task> findAllByUserId(UUID userId) {
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        return jdbcTemplate.query(sql, taskRowMapper, userId.toString());
    }

    public Task findById(UUID id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, taskRowMapper, id.toString());
    }

    public int insert(Task task) {
        String sql = "INSERT INTO tasks (id, title, description, due_date, created_at, status, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                task.getId().toString(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getCreatedAt() != null ? task.getCreatedAt() : LocalDateTime.now(),
                task.getStatus() != null ? task.getStatus().name() : Status.TODO.name(),
                task.getUser() != null ? task.getUser().getId().toString() : null
        );
    }

    public int update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, due_date = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus() != null ? task.getStatus().name() : Status.TODO.name(),
                task.getId().toString()
        );
    }

    public int deleteById(UUID id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        return jdbcTemplate.update(sql, id.toString());
    }
}