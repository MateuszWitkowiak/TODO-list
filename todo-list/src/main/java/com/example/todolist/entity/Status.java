package com.example.todolist.entity;

public enum Status {
  TODO,
  IN_PROGRESS,
  DONE;

  public String getLabel() {
    return switch (this) {
      case TODO -> "To do";
      case IN_PROGRESS -> "In progress";
      case DONE -> "Done";
    };
  }
}
