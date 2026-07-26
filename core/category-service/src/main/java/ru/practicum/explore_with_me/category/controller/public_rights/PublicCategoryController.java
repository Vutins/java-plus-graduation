package ru.practicum.explore_with_me.category.controller.public_rights;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.category.service.CategoryService;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;

import java.util.List;

@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "10") int size) {
        return categoryService.getCategories(PageRequest.of(from / size, size));
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable @Positive Long catId) {
        return categoryService.getCategoryById(catId);
    }

    @GetMapping("/list")
    public List<CategoryDto> getCategoriesByIds(@RequestParam List<Long> ids) {
        return categoryService.getCategoriesByIds(ids);
    }
}