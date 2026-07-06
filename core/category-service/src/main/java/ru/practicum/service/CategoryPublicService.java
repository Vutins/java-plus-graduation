package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.model.category.dto.CategoryDto;

import java.util.List;

public interface CategoryPublicService {

    List<CategoryDto> getListCategories(Pageable pageable);

    CategoryDto getCategoryById(Long id);
}
