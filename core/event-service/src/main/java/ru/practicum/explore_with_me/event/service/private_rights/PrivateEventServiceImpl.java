package ru.practicum.explore_with_me.event.service.private_rights;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.event.dao.Event;
import ru.practicum.explore_with_me.event.dao.Location;
import ru.practicum.explore_with_me.event.mapper.EventMapper;
import ru.practicum.explore_with_me.event.repository.EventRepository;
import ru.practicum.explore_with_me.interaction_api.exception.BadRequestException;
import ru.practicum.explore_with_me.interaction_api.exception.ConflictException;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.interaction_api.model.category.client.CategoryServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;
import ru.practicum.explore_with_me.interaction_api.model.event.EventState;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.NewEventDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.UpdateEventUserRequest;
import ru.practicum.explore_with_me.interaction_api.model.user.client.UserServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventServiceImpl implements PrivateEventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final UserServiceClient userServiceClient;
    private final CategoryServiceClient categoryServiceClient;

    private static final String serviceName = "[PrivateEventServiceImpl]";

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Pageable pageable) {

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        return events.stream()
                .map(event -> {
                    return eventMapper.toEventShortDto(
                            event,
                            categoryServiceClient.getCategoryById(event.getCategoryId()),
                            userServiceClient.getUserShortDtoClientById(event.getInitiatorId())
                    );
                })
                .toList();
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        log.debug("UserServiceClient getUserShortDtoClientById received request getEventByUser from {}", serviceName);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        UserShortDto userShortDto = userServiceClient.getUserShortDtoClientById(userId);

        CategoryDto categoryDto = categoryServiceClient.getCategoryById(event.getCategoryId());

        return eventMapper.toEventFullDto(
                event,
                categoryDto,
                userShortDto
        );
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.debug("Call getUserShortDtoClientById of user-service client from {}", serviceName);
        UserShortDto userShortDto = userServiceClient.getUserShortDtoClientById(userId);

        CategoryDto categoryDto = categoryServiceClient.getCategoryById(newEventDto.getCategory());

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("До даты мероприятия должно быть не менее 2 часов");
        }

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiatorId(userShortDto.getId());
        event.setConfirmedRequests(0);
        event.setCategoryId(categoryDto.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        if (newEventDto.getLocation() != null) {
            Location location =
                    eventMapper.toLocation(newEventDto.getLocation());
            event.setLocation(location);
        }

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toEventFullDto(
                savedEvent,
                categoryDto,
                userShortDto
        );
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.debug("UserServiceClient validateUserExistingById received request updateEventByUser from {}", serviceName);

        UserShortDto userShortDto = userServiceClient.getUserShortDtoClientById(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        CategoryDto categoryDto = categoryServiceClient.getCategoryById(event.getCategoryId());

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot update published event");
        }

        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setState(EventState.PENDING);
            } else if (updateRequest.getStateAction().equals("CANCEL_REVIEW")) {
                event.setState(EventState.CANCELED);
            }
        }

        updateEventFields(event, updateRequest);
        Event updatedEvent = eventRepository.save(event);

        return eventMapper.toEventFullDto(
                updatedEvent,
                categoryDto,
                userShortDto
        );
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategoryId(updateRequest.getCategory());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(eventMapper.toLocation(updateRequest.getLocation()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
    }
}