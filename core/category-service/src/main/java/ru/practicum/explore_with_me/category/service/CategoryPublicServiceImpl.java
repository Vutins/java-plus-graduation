package ru.practicum.explore_with_me.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.category.entity.Category;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.interaction_api.exception.ValidationException;
import ru.practicum.explore_with_me.category.mapper.CategoryMapper;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;
import ru.practicum.explore_with_me.category.repository.CategoryRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryPublicServiceImpl implements CategoryPublicService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> getListCategories(Pageable pageable) {
        log.info("получения списка категорий");
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toCategoryDto)
                .getContent();
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        log.info("получение категории по id = {}", id);
        if (id == null) {
            throw new ValidationException("id категории не может быть null");
        }
        Category categoryById = categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("категория с id = " + id + " не найдена")
        );
        return categoryMapper.toCategoryDto(categoryById);
    }
}
