package ru.practicum.explore_with_me.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.*;

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

    List<EventShortDto> getEventsByUser(Long userId, Pageable pageable);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states,
                                         List<Long> categories, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Pageable pageable);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);
}
