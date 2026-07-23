package ru.practicum.explore_with_me.event.controller.public_rights;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event.service.public_rights.PublicEventService;
import ru.practicum.explore_with_me.interaction_api.model.compilation.dto.NewCompilationDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final PublicEventService publicEventService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        return publicEventService.getEventsPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, PageRequest.of(from / size, size), httpRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable @Positive Long id,
                                     HttpServletRequest httpRequest) {
        return publicEventService.getEventById(id, httpRequest);
    }

    @GetMapping("/client/short/{id}")
    public EventShortDto getEventShortDtoByIdClient(@PathVariable @Positive Long id) {
        return publicEventService.getEventShortDtoByIdClient(id);
    }

    @GetMapping("/client/full/{id}")
    EventFullDto getEventFullDtoByIdClient(@PathVariable @Positive Long id) {
        return publicEventService.getEventFullDtoByIdClient(id);
    }

    @GetMapping("/client/validate/{eventId}")
    public void validateEventExistingById(@PathVariable @Positive Long eventId) {
        publicEventService.validateEventExistingById(eventId);
    }

    @GetMapping("/client/validate/category/{categoryId}")
    public void validateCategoryHasNoEvents(@PathVariable @Positive Long categoryId) {
        publicEventService.validateCategoryHasNoEvents(categoryId);
    }

    @GetMapping("/client/find/all")
    public Set<EventShortDto> getEventShortDtoSetByIds(@RequestParam Set<Long> eventIds) {
        return publicEventService.getEventShortDtoSetByIds(eventIds);
    }
}