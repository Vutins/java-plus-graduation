package ru.practicum.model.category.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.model.category.dto.CategoryDto;

@FeignClient(
        name = "category-service",
        path = "/categories"
)
public interface CategoryServiceClient {

    @GetMapping("/{catId}")
    CategoryDto getCategoryById(@PathVariable @Positive Long catId);
}
