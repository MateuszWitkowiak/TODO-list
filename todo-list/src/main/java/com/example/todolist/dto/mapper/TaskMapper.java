package com.example.todolist.dto.mapper;

import com.example.todolist.dto.response.CreateTaskResponse;
import com.example.todolist.dto.response.GetTaskResponse;
import com.example.todolist.entity.Task;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface TaskMapper {
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "category.id", target = "categoryId")
  CreateTaskResponse mapToCreateTaskResponse(Task task);

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "category.id", target = "categoryId")
  GetTaskResponse mapToGetTaskResponse(Task task);

  List<GetTaskResponse> mapToGetTaskResponse(List<Task> tasks);

  default Page<GetTaskResponse> mapToGetTaskResponse(Page<Task> page) {
    List<GetTaskResponse> content = mapToGetTaskResponse(page.getContent());
    return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
  }
}
