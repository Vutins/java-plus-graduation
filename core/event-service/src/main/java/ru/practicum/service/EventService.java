package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface EventService {

    List<EventShortDto> findEventsBy(PublicEventParam param, HttpServletRequest httpServletRequest);

    List<EventFullDto> findEventsBy(AdminEventParam param);

    List<EventShortDto> findEventsBy(Long id, Integer from, Integer size);

    EventFullDto findEventById(Long id, HttpServletRequest httpServletRequest);

    EventFullDto patchEvent(Long id, PatchEventDto patchEventDto);

    EventFullDto patchEventByUser(Long userId, Long eventId, PatchEventDto patchEventDto);

    EventFullDto findEventByIdAndUser(Long userId, Long eventId);

    EventFullDto saveNewEvent(Long userId, NewEventDto newEventDto);
}
