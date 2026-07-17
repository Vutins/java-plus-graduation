package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.entityparam.PublicEventParam;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.service.EventService;

import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> findEventsBy(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest httpServletRequest
    ) {
        PublicEventParam param = new PublicEventParam(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return ResponseEntity.ok(eventService.findEventsBy(param, httpServletRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> findEventById(@PathVariable @Min(1) Long id, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(eventService.findEventById(id, httpServletRequest));
    }

    @GetMapping("/client/full/{id}")
    public EventFullDto fullDtoFindById(@PathVariable @Positive Long id, HttpServletRequest httpServletRequest) {
        return eventService.findEventById(id, httpServletRequest);
    }

    @GetMapping("/client/short/{id}")
    public EventShortDto getEventShortDtoByIdClient(@PathVariable @Positive Long id) {
        return eventService.getEventShortDtoByIdClient(id);
    }

    @GetMapping("/client/validate/{eventId}")
    public void validateEventExistingById(@PathVariable @Positive Long eventId) {
        eventService.validateEventExistingById(eventId);
    }

    @GetMapping("/client/validate/category/{categoryId}")
    public void validateCategoryHasNoEvents(@PathVariable @Positive Long categoryId) {
        eventService.validateCategoryHasNoEvents(categoryId);
    }

    @GetMapping("/client/find/all")
    public Set<EventShortDto> getEventShortDtoSetByIds(@RequestParam Set<Long> eventIds) {
        return eventService.getEventShortDtoSetByIds(eventIds);
    }
}
