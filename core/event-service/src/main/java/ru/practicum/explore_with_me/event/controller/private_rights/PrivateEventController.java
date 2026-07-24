package ru.practicum.explore_with_me.event.controller.private_rights;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.event.service.EventService;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.NewEventDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.UpdateEventUserRequest;

import java.util.List;

@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsByUser(@PathVariable @Positive Long userId,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        return eventService.getEventsByUser(userId, PageRequest.of(from / size, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable @Positive Long userId,
                                    @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUser(@PathVariable @Positive Long userId,
                                       @PathVariable @Positive Long eventId) {
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(@PathVariable @Positive Long userId,
                                          @PathVariable @Positive Long eventId,
                                          @RequestBody @Valid UpdateEventUserRequest updateRequest) {
        return eventService.updateEventByUser(userId, eventId, updateRequest);
    }
}