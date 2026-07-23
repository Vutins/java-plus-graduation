package ru.practicum.explore_with_me.interaction_api.model.event.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;

import java.util.Set;

@FeignClient(
        name = "event-service",
        path = "/events"
)
public interface EventServiceClient {
    @RequestMapping(
            method = RequestMethod.HEAD,
            value = "/categories/{catId}/exists"
    )
    Boolean categoryHasEvents(@PathVariable Long catId);

    @GetMapping("/client/short/{id}")
    EventShortDto getEventShortDtoByIdClient(@PathVariable @Positive Long id);

    @GetMapping("/client/full/{id}")
    EventFullDto getEventFullDtoByIdClient(@PathVariable @Positive Long id);

    @GetMapping("/client/validate/{eventId}")
    void validateEventExistingById(@PathVariable @Positive Long eventId);

    @GetMapping("/client/validate/category/{categoryId}")
    void validateCategoryHasNoEvents(
            @PathVariable @Positive Long categoryId);

    @GetMapping("/client/find/all")
    Set<EventShortDto> getEventShortDtoSetByIds(@RequestParam Set<Long> eventIds);
}