package com.example.todolist.dto.mapper;

import com.example.todolist.dto.response.CreateCategoryResponse;
import com.example.todolist.dto.response.GetCategoryResponse;
import com.example.todolist.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CategoryMapper {
    @Mapping(source="user.id", target = "userId")
    CreateCategoryResponse mapToCreateCategoryResponse(Category category);

    @Mapping(source="user.id", target = "userId")
    GetCategoryResponse mapToGetCategoryResponse(Category category);

    List<GetCategoryResponse> mapToGetCategoryResponse(List<Category> categories);
}
