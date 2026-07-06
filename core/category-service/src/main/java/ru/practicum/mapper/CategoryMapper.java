package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.entity.Category;
import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.category.dto.CategoryRequestDto;


@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toCategory(CategoryRequestDto categoryRequestDto);

    CategoryDto toCategoryDto(Category category);
}
