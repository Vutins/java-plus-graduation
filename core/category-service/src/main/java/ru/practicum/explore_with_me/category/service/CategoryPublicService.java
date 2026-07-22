package ru.practicum.explore_with_me.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;

import java.util.List;

public interface CategoryPublicService {

    List<CategoryDto> getListCategories(Pageable pageable);

    CategoryDto getCategoryById(Long id);
}
