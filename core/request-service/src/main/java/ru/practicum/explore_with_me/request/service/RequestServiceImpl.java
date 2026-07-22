package ru.practicum.explore_with_me.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.request.entity.ParticipationRequest;
import ru.practicum.explore_with_me.interaction_api.exception.ConflictException;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.interaction_api.exception.ValidationException;
import ru.practicum.explore_with_me.request.mapper.RequestMapper;
import ru.practicum.explore_with_me.interaction_api.model.event.client.EventServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.enums.State;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;
import ru.practicum.explore_with_me.interaction_api.model.request.enums.RequestStatus;
import ru.practicum.explore_with_me.interaction_api.model.user.client.UserServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserDto;
import ru.practicum.explore_with_me.request.repository.RequestRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение всех запросов пользователя с id = {}", userId);

        userServiceClient.validateUserExistingById(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        return requestMapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Добавление запроса от пользователя {} на событие {}", userId, eventId);

        if (eventId == null) {
            throw new ValidationException("Параметр eventId отсутствует.");
        }

        UserDto requester = userServiceClient.getUserById(userId);

        EventFullDto event = eventServiceClient.getEventFullDtoByIdClient(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Нельзя добавить повторный запрос на участие в событии");
        }

        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() != 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников для данного события");
        }

        RequestStatus status;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }

        ParticipationRequest request = requestMapper.toEntity(requester.getId(), event.getId(), status);
        ParticipationRequest savedRequest = requestRepository.save(request);

        return requestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса с id = {} пользователем {}", requestId, userId);

        userServiceClient.validateUserExistingById(userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос с id = %d не найден", requestId)));

        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundException(String.format("Запрос с id = %d не найден у пользователя %d", requestId, userId));
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest canceledRequest = requestRepository.save(request);

        return requestMapper.toDto(canceledRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.info("Получение запросов на участие в событии {} для пользователя {}", eventId, userId);

       userServiceClient.validateUserExistingById(userId);

        EventFullDto event = eventServiceClient.getEventFullDtoByIdClient(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException(String.format("Событие с id = %d не принадлежит пользователю %d", eventId,
                    userId));
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        return requestMapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса заявок для события {} пользователем {}", eventId, userId);

        userServiceClient.validateUserExistingById(userId);

        EventFullDto event = eventServiceClient.getEventFullDtoByIdClient(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException(String.format("Событие с id = %d не принадлежит пользователю %d", eventId, userId));
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Нельзя изменять статус заявок для неопубликованного события");
        }

        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        Integer participantLimit = event.getParticipantLimit();

        if (confirmedRequests >= participantLimit) {
            throw new ConflictException("Достигнут лимит одобренных заявок");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByIdInAndEventId(request.getRequestIds(), eventId);

        for (ParticipationRequest req : requests) {
            if (!req.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
            }
        }

        List<ParticipationRequest> confirmedRequestsList = new ArrayList<>();
        List<ParticipationRequest> rejectedRequestsList = new ArrayList<>();

        if (participantLimit == 0 || !event.getRequestModeration()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(new ArrayList<>())
                    .rejectedRequests(new ArrayList<>())
                    .build();
        }

        if (request.getStatus().equals("CONFIRMED")) {
            for (ParticipationRequest req : requests) {
                if (confirmedRequests < participantLimit) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequestsList.add(req);
                    confirmedRequests++;
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejectedRequestsList.add(req);
                }
            }

            if (confirmedRequests >= participantLimit) {
                requestRepository.rejectAllPendingRequestsByEventId(eventId);
            }
        } else if (request.getStatus().equals("REJECTED")) {
            for (ParticipationRequest req : requests) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequestsList.add(req);
            }
        }

        requestRepository.saveAll(confirmedRequestsList);
        requestRepository.saveAll(rejectedRequestsList);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requestMapper.toDtoList(confirmedRequestsList))
                .rejectedRequests(requestMapper.toDtoList(rejectedRequestsList))
                .build();
    }

    @Override
    public Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(
            List<Long> eventIds,
            RequestStatus requestStatus) {
        List<ParticipationRequest> requests = requestRepository.findAllByEventIdInAndStatus(eventIds, requestStatus);
        return requests.stream().map(requestMapper::toDto).collect(Collectors.groupingBy(ParticipationRequestDto::getEvent));
    }

    @Override
    public ParticipationRequestDto getUserRequestByUserIdAndEventId(Long userId, Long eventId) {
        ParticipationRequest request = requestRepository.findByEventIdAndRequesterId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Заявки юзера с id=" + userId + " на участие в ивенте " +
                        "с id=" + eventId + " нет в БД!"));
        return requestMapper.toDto(request);
    }

    @Override
    public Long getConfirmedRequestsCountByEventId(Long eventId) {
        eventServiceClient.validateEventExistingById(eventId);
        Long count = requestRepository.countConfirmedRequestsByEventId(eventId);
        return count != null ? count : 0L;
    }

    @Override
    public List<Object[]> countConfirmedRequestsForEvents(List<Long> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }
        events.forEach(eventServiceClient::validateEventExistingById);

        return requestRepository.countConfirmedRequestsForEvents(events);
    }
}