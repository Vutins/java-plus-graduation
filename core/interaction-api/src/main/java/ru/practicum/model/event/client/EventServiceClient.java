package ru.practicum.model.event.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.model.event.dto.EventFullDto;

@FeignClient(
        name = "event-service",
        path = "/events"
)
public interface EventServiceClient {

    @GetMapping("/client/validate/category/{categoryId}")
    void existsByCategoryId(@PathVariable @Positive Long categoryId);

    @GetMapping("/client/full/{id}")
    EventFullDto fullDtoFindById(@PathVariable @Positive Long id);
}
