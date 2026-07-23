package ru.practicum.explore_with_me.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explore_with_me.category.dao.Category;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.NewCategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toCategoryDto(Category category);

    Category toCategory(CategoryDto categoryDto);

    Category toCategoryFromNew(NewCategoryDto newCategoryDto);
}