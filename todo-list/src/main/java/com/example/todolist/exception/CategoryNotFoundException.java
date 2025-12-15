package com.example.todolist.exception;

public class CategoryNotFoundException extends RuntimeException {
  public CategoryNotFoundException(String fieldName, Object fieldValue) {
    super(String.format("Task not found with %s : '%s'", fieldName, fieldValue));
  }
}
