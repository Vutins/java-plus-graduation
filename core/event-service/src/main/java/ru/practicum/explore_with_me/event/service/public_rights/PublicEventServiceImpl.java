package ru.practicum.explore_with_me.event.service.public_rights;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.EndpointHitDto;
import ru.practicum.explore_with_me.StatResponseDto;
import ru.practicum.explore_with_me.StatsClient;
import ru.practicum.explore_with_me.event.dao.Event;
import ru.practicum.explore_with_me.event.mapper.EventMapper;
import ru.practicum.explore_with_me.event.repository.EventRepository;
import ru.practicum.explore_with_me.interaction_api.exception.BadRequestException;
import ru.practicum.explore_with_me.interaction_api.exception.ConflictException;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.interaction_api.model.category.client.CategoryServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;
import ru.practicum.explore_with_me.interaction_api.model.event.EventState;
import ru.practicum.explore_with_me.interaction_api.model.user.client.UserServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final UserServiceClient userServiceClient;
    private final CategoryServiceClient categoryServiceClient;

    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Pageable pageable,
                                               HttpServletRequest request) {
        verifyRange(rangeStart, rangeEnd);
        statsClient.hit(EndpointHitDto
                .builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        Specification<Event> spec = Specification.where(null);

        if (text != null && !text.isBlank())
            spec = spec.and(searchText(text.toLowerCase()));

        if (categories != null && !categories.isEmpty())
            spec = spec.and(searchCategoryIn(categories));

        spec = spec.and(searchAfterDate(rangeStart));

        if (rangeEnd != null)
            spec = spec.and(searchBeforeDate(rangeEnd));

        if (onlyAvailable)
            spec = spec.and(searchAvailable());

        spec = spec.and(searchPublished());

        List<Event> results = eventRepository.findAll(spec, pageable).toList();
        Map<Long, Long> views = getEventsViews(results);

        return results
                .stream()
                .map(event -> {
                    return eventMapper.toEventShortDto(
                            event,
                            categoryServiceClient.getCategoryById(event.getCategoryId()),
                            userServiceClient.getUserShortDtoClientById(event.getInitiatorId()));
                })
                .peek(dto -> dto.setViews(views.getOrDefault(dto.getId(), 0L)))
                .toList();
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

        UserShortDto userShortDto = userServiceClient.getUserShortDtoClientById(event.getInitiatorId());

        CategoryDto categoryDto = categoryServiceClient.getCategoryById(event.getCategoryId());

        return eventMapper.toEventShortDto(event, categoryDto, userShortDto);
    }

    @Override
    public Set<EventShortDto> getEventShortDtoSetByIds(Set<Long> eventIds) {
        return eventRepository.findAllByIdIn(eventIds)
                .stream()
                .map(event -> {
                    return eventMapper.toEventShortDto(
                            event,
                            categoryServiceClient.getCategoryById(event.getCategoryId()),
                            userServiceClient.getUserShortDtoClientById(event.getInitiatorId()));
                })
                .collect(Collectors.toSet());
    }

    @Override
    public EventFullDto getEventFullDtoByIdClient(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with the same id not found"));

        UserShortDto userShortDto = userServiceClient.getUserShortDtoClientById(event.getInitiatorId());

        CategoryDto categoryDto = categoryServiceClient.getCategoryById(event.getCategoryId());

        return eventMapper.toEventFullDto(
                event,
                categoryDto,
                userShortDto
        );
    }

    @Override
    @Transactional
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        UserShortDto userShortDto = userServiceClient.getUserShortDtoClientById(event.getInitiatorId());

        CategoryDto categoryDto = categoryServiceClient.getCategoryById(event.getCategoryId());

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not found");
        }

        statsClient.hit(EndpointHitDto
                .builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        EventFullDto eventDto = eventMapper.toEventFullDto(
                event,
                categoryDto,
                userShortDto
        );

        Map<Long, Long> views = getEventsViews(List.of(event));
        eventDto.setViews(views.getOrDefault(id, 0L));

        return eventDto;
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        List<String> uris = events
                .stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .toList();

        LocalDateTime startDate = events
                .stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));

        Map<Long, Long> viewStats = new HashMap<>();
        try {
            LocalDateTime endDate = LocalDateTime.now();
            List<StatResponseDto> stats = statsClient.getStats(startDate, endDate,
                    uris, true);
            viewStats = stats
                    .stream()
                    .filter(s -> s.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            s -> Long.parseLong(s.getUri().substring("/events/".length())),
                            StatResponseDto::getHits
                    ));
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
        }
        return viewStats;
    }

    private void verifyRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new BadRequestException("Дата начала ивента не может быть позже даты окончания");
            }
        }
    }

    private Specification<Event> searchText(String text) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text + "%")
                );
    }

    private Specification<Event> searchCategoryIn(List<Long> categories) {
        return (root, query, criteriaBuilder) ->
                root.get("categoryId").in(categories);
    }

    private Specification<Event> searchAfterDate(LocalDateTime rangeStart) {
        LocalDateTime start = Objects.requireNonNullElse(rangeStart, LocalDateTime.now());
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), start);
    }

    private Specification<Event> searchBeforeDate(LocalDateTime rangeEnd) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd);
    }

    private Specification<Event> searchAvailable() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0);
    }

    private Specification<Event> searchPublished() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED);
    }
}