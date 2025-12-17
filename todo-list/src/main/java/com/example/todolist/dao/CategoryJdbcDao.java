package com.example.todolist.dao;

import com.example.todolist.entity.Category;
import com.example.todolist.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CategoryJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public CategoryJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Category> categoryRowMapper = (rs, rowNum) -> {
        Category category = new Category();
        category.setId(UUID.fromString(rs.getString("id")));
        category.setName(rs.getString("name"));
        category.setColor(rs.getString("color"));
        String userIdStr = rs.getString("user_id");
        if (userIdStr != null) {
            User user = new User();
            user.setId(UUID.fromString(userIdStr));
            category.setUser(user);
        }
        return category;
    };

    public List<Category> findAllByUserId(UUID userId) {
        String sql = "SELECT * FROM categories WHERE user_id = ?";
        return jdbcTemplate.query(sql, categoryRowMapper, userId.toString());
    }

    public Category findById(UUID id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, categoryRowMapper, id.toString());
    }

    public int insert(Category category) {
        String sql = "INSERT INTO categories (id, name, color, user_id) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                category.getId().toString(),
                category.getName(),
                category.getColor(),
                category.getUser() != null ? category.getUser().getId().toString() : null
        );
    }

    public int update(Category category) {
        String sql = "UPDATE categories SET name = ?, color = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                category.getName(),
                category.getColor(),
                category.getId().toString()
        );
    }

    public int deleteById(UUID id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        return jdbcTemplate.update(sql, id.toString());
    }
}