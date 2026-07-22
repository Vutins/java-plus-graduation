package ru.practicum.explore_with_me.event.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.event.entity.Event;
import ru.practicum.explore_with_me.interaction_api.model.category.client.CategoryServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.NewEventDto;
import ru.practicum.explore_with_me.interaction_api.model.event.enums.State;
import ru.practicum.explore_with_me.interaction_api.model.user.client.UserServiceClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocationMapper locationMapper;
    private final CategoryServiceClient categoryServiceClient;
    private final UserServiceClient userServiceClient;

    public EventFullDto toFullDto(Event event) {
        EventFullDto eventFullDto = EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryServiceClient.getCategoryById(event.getCategoryId()))
                .description(event.getDescription())
                .initiator(userServiceClient.getUserShortById(event.getInitiatorId()))
                .location(locationMapper.toDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(String.valueOf(event.getState()))
                .title(event.getTitle())
                .build();

        if (event.getCreatedOn() != null) {
            eventFullDto.setCreatedOn(formatter.format(event.getCreatedOn()));
        }

        if (event.getEventDate() != null) {
            eventFullDto.setEventDate(formatter.format(event.getEventDate()));
        }

        if (event.getPublishedOn() != null) {
            eventFullDto.setPublishedOn(formatter.format(event.getPublishedOn()));
        }

        return eventFullDto;
    }

    public List<EventShortDto> toListShortDtoWithViewsAndRequests(
            List<Event> events, Map<Long, Long> viewsForEvents, Map<Long, Long> requests) {
        return events.stream()
                .map(e -> {
                    EventShortDto eventShortDto = toShortDto(e);
                    eventShortDto.setViews(viewsForEvents.getOrDefault(e.getId(), 0L));
                    eventShortDto.setConfirmedRequests(requests.getOrDefault(e.getId(), 0L));
                    return eventShortDto;
                })
                .toList();
    }

    public List<EventFullDto> toListFullDtoWithViewsAndRequests(
            List<Event> events, Map<Long, Long> viewsForEvents, Map<Long, Long> requests) {
        return events.stream()
                .map(e -> {
                    EventFullDto eventFullDto = toFullDto(e);
                    eventFullDto.setViews(viewsForEvents.getOrDefault(e.getId(), 0L));
                    eventFullDto.setConfirmedRequests(requests.getOrDefault(e.getId(), 0L));
                    return eventFullDto;
                })
                .toList();
    }

    public EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryServiceClient.getCategoryById(event.getCategoryId()))
                .eventDate(formatter.format(event.getEventDate()))
                .initiator(userServiceClient.getUserShortById(event.getInitiatorId()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();
    }

    public Event toEntity(NewEventDto newEventDto, Long initiatorId, Long categoryId) {
        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .categoryId(categoryId)
                .initiatorId(initiatorId)
                .location(locationMapper.toLocation(newEventDto.getLocation()))
                .description(newEventDto.getDescription())
                .createdOn(LocalDateTime.now())
                .eventDate(LocalDateTime.parse(newEventDto.getEventDate(), formatter))
                .state(State.PENDING)
                .title(newEventDto.getTitle())
                .build();

        if (newEventDto.getPaid() != null) {
            event.setPaid(newEventDto.getPaid());
        } else {
            event.setPaid(false);
        }

        if (newEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(newEventDto.getParticipantLimit());
        } else {
            event.setParticipantLimit(0);
        }

        if (newEventDto.getRequestModeration() != null) {
            event.setRequestModeration(newEventDto.getRequestModeration());
        } else {
            event.setRequestModeration(true);
        }

        return event;
    }
}
