package ru.practicum.explore_with_me.interaction_api.model.category.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;

import java.util.List;

@FeignClient(
        name = "category-service",
        path = "/categories"
)
public interface CategoryServiceClient {

    @GetMapping("/{catId}")
    CategoryDto getCategoryById(@PathVariable @Positive Long catId);

    @GetMapping("/list")
    List<CategoryDto> getCategoriesByIds(@RequestParam("ids") List<Long> ids);
}
