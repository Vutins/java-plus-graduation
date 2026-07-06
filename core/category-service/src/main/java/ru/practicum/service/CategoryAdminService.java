package ru.practicum.service;

import ru.practicum.model.category.dto.CategoryDto;
import ru.practicum.model.category.dto.CategoryRequestDto;

public interface CategoryAdminService {

    CategoryDto create(CategoryRequestDto categoryRequestDto);

    CategoryDto update(Long id, CategoryRequestDto categoryRequestDto);

    void delete(Long id);
}
