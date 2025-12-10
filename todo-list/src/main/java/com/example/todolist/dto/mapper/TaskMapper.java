package com.example.todolist.dto.mapper;

import com.example.todolist.dto.response.CreateTaskResponse;
import com.example.todolist.dto.response.GetTaskResponse;
import com.example.todolist.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface TaskMapper {
    @Mapping(source="user.id", target="userId")
    @Mapping(source="category.id", target = "categoryId")
    CreateTaskResponse mapToCreateTaskResponse(Task task);

    @Mapping(source="user.id", target="userId")
    @Mapping(source="category.id", target = "categoryId")
    GetTaskResponse mapToGetTaskResponse(Task task);

    List<GetTaskResponse> mapToGetTaskResponse(List<Task> tasks);
}
