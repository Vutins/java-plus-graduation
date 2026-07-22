package ru.practicum.explore_with_me.category.service;

import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryRequestDto;

public interface CategoryAdminService {

    CategoryDto create(CategoryRequestDto categoryRequestDto);

    CategoryDto update(Long id, CategoryRequestDto categoryRequestDto);

    void delete(Long id);
}
