package ru.practicum.explore_with_me.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.explore_with_me.StatsClient;
import ru.practicum.explore_with_me.ViewStatsDto;
import ru.practicum.explore_with_me.event.entity.Event;
import ru.practicum.explore_with_me.event.entity.Location;
import ru.practicum.explore_with_me.event.entityparam.AdminEventParam;
import ru.practicum.explore_with_me.event.entityparam.PublicEventParam;
import ru.practicum.explore_with_me.interaction_api.exception.ConflictException;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.event.mapper.EventMapper;
import ru.practicum.explore_with_me.interaction_api.model.category.client.CategoryServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.NewEventDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.PatchEventDto;
import ru.practicum.explore_with_me.interaction_api.model.event.enums.SortType;
import ru.practicum.explore_with_me.interaction_api.model.event.enums.State;
import ru.practicum.explore_with_me.interaction_api.model.request.client.RequestServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.user.client.UserServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserDto;
import ru.practicum.explore_with_me.event.repository.EventRepository;
import ru.practicum.explore_with_me.event.specification.AdminEventSpecification;
import ru.practicum.explore_with_me.event.specification.EventSpecification;
import ru.practicum.explore_with_me.event.specification.PublicEventSpecification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserServiceClient userServiceClient;
    private final CategoryServiceClient categoryServiceClient;
    private final RequestServiceClient requestServiceClient;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> findEventsBy(PublicEventParam param, HttpServletRequest httpServletRequest) {
        saveHit(httpServletRequest);

        EventSpecification specification = PublicEventSpecification.builder()
                .text(param.getText())
                .categories(param.getCategories())
                .paid(param.getPaid())
                .onlyAvailable(param.getOnlyAvailable())
                .rangeStart(param.getRangeStart())
                .rangeEnd(param.getRangeEnd())
                .build();

        Pageable pageable = PageRequest.of(param.getFrom(), param.getSize());

        if (param.getSort() != null && param.getSort().isBlank()) {
            if (String.valueOf(SortType.EVENT_DATE).equals(param.getSort())) {
                Sort sort = Sort.by(Sort.Direction.DESC, param.getSort());
                pageable = PageRequest.of(param.getFrom(), param.getSize(), sort);
                Page<Event> events = eventRepository.findAll(specification.toSpecification(), pageable);
                return events.stream()
                        .map(eventMapper::toShortDto)
                        .toList();
            }
        }

        List<Event> events = eventRepository.findAll(specification.toSpecification(), pageable).getContent();

        Map<Long, Long> viewsForEvents = getViews(events);
        Map<Long, Long> requestsForEvents = getRequestsForEvents(events);
        System.out.println(requestsForEvents.toString());

        List<EventShortDto> eventsDto = eventMapper.toListShortDtoWithViewsAndRequests(events, viewsForEvents, requestsForEvents);

        if (param.getSort() != null && !param.getSort().isBlank()) {
            if (String.valueOf(SortType.VIEWS).equals(param.getSort())) {
                eventsDto.sort(Comparator.comparing(EventShortDto::getViews).reversed());
            }
        }

        return eventsDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> findEventsBy(AdminEventParam param) {
        EventSpecification specification = AdminEventSpecification.builder()
                .users(param.getUsers())
                .states(param.getStates())
                .categories(param.getCategories())
                .rangeStart(param.getRangeStart())
                .rangeEnd(param.getRangeEnd())
                .build();

        Pageable pageable = PageRequest.of(param.getFrom(), param.getSize());

        List<Event> events = eventRepository.findAll(specification.toSpecification(), pageable).getContent();

        Map<Long, Long> viewsForEvents = getViews(events);
        Map<Long, Long> requestsForEvents = getRequestsForEvents(events);

        return eventMapper.toListFullDtoWithViewsAndRequests(events, viewsForEvents, requestsForEvents);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto findEventById(Long id, HttpServletRequest httpServletRequest) {
        log.info("Получение пользователя по id.");
        Event event = eventRepository.findPublishedEventById(id)
                .orElseThrow(() -> new NotFoundException(String.format("События с id = %d не существует.", id)));
        log.info("Информация о событии получена.");
        saveHit(httpServletRequest);

        EventFullDto eventFullDto = eventMapper.toFullDto(event);

        log.info("Получаем количество просмотров.");
        eventFullDto.setViews(getStats(event));
        eventFullDto.setConfirmedRequests(requestServiceClient.getConfirmedRequestsCountByEventId(id));
        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto patchEvent(Long id, PatchEventDto patchEventDto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d отсутствует.", id)));

        if (!event.getState().equals(State.PENDING)) {
            throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации.");
        }

        if (patchEventDto.getStateAction() != null) {
            switch (patchEventDto.getStateAction()) {
                case "PUBLISH_EVENT" -> {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    eventRepository.save(event);
                }

                case "REJECT_EVENT" -> {
                    event.setState(State.CANCELED);
                    eventRepository.save(event);
                }
            }
        }

        patchFieldValidation(event, patchEventDto);
        EventFullDto eventFullDto = eventMapper.toFullDto(event);

        if (event.getPublishedOn() != null) {
            eventFullDto.setViews(getStats(event));

            if (event.getEventDate().plusHours(1).isBefore(event.getPublishedOn())) {
                throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации. " +
                        "Дата события: " + event.getEventDate() + ", дата публикации: " + event.getPublishedOn());
            }
        }

        eventFullDto.setConfirmedRequests(requestServiceClient.getConfirmedRequestsCountByEventId(id));

        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto patchEventByUser(Long userId, Long eventId, PatchEventDto patchEventDto) {
        userServiceClient.validateUserExistingById(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d отсутствует.", eventId)));

        if (!(event.getState().equals(State.PENDING) || event.getState().equals(State.CANCELED))) {
            throw new ConflictException("События со статусом PUBLISHED не могут быть изменены.");
        }

        if (patchEventDto.getStateAction() != null) {
            switch (patchEventDto.getStateAction()) {
                case "CANCEL_REVIEW" -> {
                    event.setState(State.CANCELED);
                    eventRepository.save(event);
                }

                case "SEND_TO_REVIEW" -> {
                    event.setState(State.PENDING);
                    eventRepository.save(event);
                }
            }
        }

        EventFullDto eventFullDto = eventMapper.toFullDto(event);

        if (event.getPublishedOn() != null) {
            eventFullDto.setViews(getStats(event));
        }

        return eventFullDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> findEventsBy(Long userId, Integer from, Integer size) {
        userServiceClient.validateUserExistingById(userId);

        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAll(pageable).getContent();

        Map<Long, Long> viewsForEvents = getViews(events);
        Map<Long, Long> requestsForEvents = getRequestsForEvents(events);

        return eventMapper.toListShortDtoWithViewsAndRequests(events, viewsForEvents, requestsForEvents);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto findEventByIdAndUser(Long userId, Long eventId) {
        userServiceClient.validateUserExistingById(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d отсутствует.", eventId)));

        EventFullDto eventFullDto = eventMapper.toFullDto(event);

        if (event.getPublishedOn() != null) {
            eventFullDto.setViews(getStats(event));
        }

        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto saveNewEvent(Long userId, NewEventDto newEventDto) {
        UserDto user = userServiceClient.getUserById(userId);

        CategoryDto category = categoryServiceClient.getCategoryById(newEventDto.getCategory());
        Event event = eventMapper.toEntity(newEventDto, user.getId(), category.getId());
        Event createdEvent = eventRepository.save(event);

        return eventMapper.toFullDto(createdEvent);
    }

    private Map<Long, Long> getRequestsForEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventsIds = events.stream().map(Event::getId).toList();

        List<Object[]> results = requestServiceClient.countConfirmedRequestsForEvents(eventsIds);

        return results.stream()
                .collect(Collectors.toMap(
                        o -> (Long) o[0],
                        o -> (Long) o[1]
                ));
    }

    private void saveHit(HttpServletRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            statsClient.saveHit(new EndpointHitDto(
                    null,
                    "main-service",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now().format(formatter)
            ));
        } catch (Exception e) {
            log.warn("Произошла ошибка при сохранении статистики.");
            throw new RuntimeException(e);
        }
    }

    private Long getStats(Event event) {
        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    event.getPublishedOn(),
                    LocalDateTime.now(),
                    List.of("/events/" + event.getId()),
                    true);

            return stats.isEmpty() ? 0L : stats.getFirst().getHits();
        } catch (Exception e) {
            log.warn("Произошла ошибка при получении статистики.");
            throw new RuntimeException(e);
        }
    }

    private Map<Long, Long> getViews(List<Event> events) {
        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        List<ViewStatsDto> stats = statsClient.getStats(
                LocalDateTime.now(),
                LocalDateTime.now(),
                uris,
                true);

        Pattern pattern = Pattern.compile("/events/(\\d+)");
        return stats.stream().collect(Collectors.toMap(s ->
                Long.parseLong(String.valueOf(pattern.matcher(s.getUri()).find())), ViewStatsDto::getHits));
    }

    private void patchFieldValidation(Event event, PatchEventDto patchEventDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (patchEventDto.getCategory() != null) {
            CategoryDto category = categoryServiceClient.getCategoryById(patchEventDto.getCategory());
            event.setCategoryId(category.getId());
        }

        if (patchEventDto.getLocation() != null) {
            Location location = event.getLocation();
            location.setLat(patchEventDto.getLocation().getLat());
            location.setLon(patchEventDto.getLocation().getLon());
            event.setLocation(location);
        }

        if (patchEventDto.getAnnotation() != null) {
            event.setAnnotation(patchEventDto.getAnnotation());
        }

        if (patchEventDto.getDescription() != null) {
            event.setDescription(patchEventDto.getDescription());
        }

        if (patchEventDto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(patchEventDto.getEventDate(), formatter));
        }

        if (patchEventDto.getPaid() != null) {
            event.setPaid(patchEventDto.getPaid());
        }

        if (patchEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(patchEventDto.getParticipantLimit());
        }

        if (patchEventDto.getRequestModeration() != null) {
            event.setRequestModeration(patchEventDto.getRequestModeration());
        }

        if (patchEventDto.getTitle() != null) {
            event.setTitle(patchEventDto.getTitle());
        }
    }

    public void existsByCategoryId(Long categoryId) {
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("удаление не возможно пока существуют события с этой категорией");
        }
    }

    @Override
    public void validateCategoryHasNoEvents(Long categoryId) {
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Category has associated events and cannot be deleted");
        }
    }

    @Override
    public void validateEventExistingById(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Ивента с id=" + eventId + " нет в БД!");
        }
    }

    @Override
    public EventShortDto getEventShortDtoByIdClient(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with the same id not found"));
        return eventMapper.toShortDto(event);
    }

    @Override
    public Set<EventShortDto> getEventShortDtoSetByIds(Set<Long> eventIds) {
        return eventRepository.findAllByIdIn(eventIds)
                .stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toSet());
    }

    @Override
    public EventFullDto getEventFullDtoByIdClient(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with the same id not found"));
        return eventMapper.toFullDto(event);
    }
}
