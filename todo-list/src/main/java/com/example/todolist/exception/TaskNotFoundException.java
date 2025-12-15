package com.example.todolist.exception;

public class TaskNotFoundException extends RuntimeException {
  public TaskNotFoundException(String fieldName, Object fieldValue) {
    super(String.format("Task not found with %s : '%s'", fieldName, fieldValue));
  }
}
