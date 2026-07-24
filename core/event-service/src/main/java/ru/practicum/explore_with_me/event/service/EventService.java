package ru.practicum.explore_with_me.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventService {

    List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort, Pageable pageable,
                                        HttpServletRequest httpServletRequest);

    EventFullDto getEventById(Long id, HttpServletRequest httpServletRequest);

    EventShortDto getEventShortDtoByIdClient(Long id);

    Set<EventShortDto> getEventShortDtoSetByIds(Set<Long> eventIds);

    EventFullDto getEventFullDtoByIdClient(Long id);

    void validateEventExistingById(Long eventId);

    void validateCategoryHasNoEvents(Long categoryId);
}
