package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.entityparam.AdminEventParam;
import ru.practicum.entityparam.PublicEventParam;
import ru.practicum.model.event.dto.EventFullDto;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.model.event.dto.NewEventDto;
import ru.practicum.model.event.dto.PatchEventDto;

import java.util.List;
import java.util.Set;

public interface EventService {

    List<EventShortDto> findEventsBy(PublicEventParam param, HttpServletRequest httpServletRequest);

    List<EventFullDto> findEventsBy(AdminEventParam param);

    List<EventShortDto> findEventsBy(Long id, Integer from, Integer size);

    EventFullDto findEventById(Long id, HttpServletRequest httpServletRequest);

    EventFullDto patchEvent(Long id, PatchEventDto patchEventDto);

    EventFullDto patchEventByUser(Long userId, Long eventId, PatchEventDto patchEventDto);

    EventFullDto findEventByIdAndUser(Long userId, Long eventId);

    EventFullDto saveNewEvent(Long userId, NewEventDto newEventDto);

    void existsByCategoryId(Long categoryId);

    EventShortDto getEventShortDtoByIdClient(Long id);

    Set<EventShortDto> getEventShortDtoSetByIds(Set<Long> eventIds);

    EventFullDto getEventFullDtoByIdClient(Long id);

    void validateEventExistingById(Long eventId);

    void validateCategoryHasNoEvents(Long categoryId);

}
