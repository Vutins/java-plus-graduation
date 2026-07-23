package ru.practicum.explore_with_me.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.interaction_api.exception.ConflictException;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.interaction_api.model.event.EventState;
import ru.practicum.explore_with_me.interaction_api.model.event.client.EventServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;
import ru.practicum.explore_with_me.interaction_api.model.request.RequestStatus;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;
import ru.practicum.explore_with_me.interaction_api.model.user.client.UserServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;
import ru.practicum.explore_with_me.request.dao.ParticipationRequest;
import ru.practicum.explore_with_me.request.mapper.ParticipationRequestMapper;
import ru.practicum.explore_with_me.request.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements RequestService {
    private final ParticipationRequestRepository participationRequestRepository;

    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;

    private final ParticipationRequestMapper participationRequestMapper;

    private final static String serviceName = "[REQUEST_SERVICE]";

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.debug("Запрос на получение event клиентом из getEventRequests сервиса {}", serviceName);
        EventShortDto event = eventServiceClient.getEventShortDtoByIdClient(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        return participationRequestRepository.findByEventId(eventId).stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.debug("UserServiceClient validateUserExistingById received request getUserRequests from {}", serviceName);
        userServiceClient.validateUserExistingById(userId);

        return participationRequestRepository.findByRequesterId(userId).stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto getUserRequestByUserIdAndEventId(Long userId, Long eventId) {
        ParticipationRequest request = participationRequestRepository.findByEventIdAndRequesterId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Заявки юзера с id=" + userId + " на участие в ивенте " +
                        "с id=" + eventId + " нет в БД!"));
        return participationRequestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.debug("Call getUserShortDtoClientById of user-service client from {}", serviceName);
        UserShortDto user = userServiceClient.getUserShortDtoClientById(userId);

        log.debug("Запрос на получение event клиентом из createRequest сервиса {}", serviceName);
        EventFullDto event = eventServiceClient.getEventFullDtoByIdClient(eventId);

        participationRequestRepository.findByEventIdAndRequesterId(eventId, userId)
                .ifPresent(existingRequest -> {
                    throw new ConflictException("Request from user with id=" + userId +
                            " to event with id=" + eventId + " already exists");
                });

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in their own event");
        }

        if (!EventState.PUBLISHED.toString().equals(event.getState())) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (event.getParticipantLimit() > 0) {
            Long confirmedCount = participationRequestRepository.countConfirmedRequestsByEventId(eventId);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached for event with id=" + eventId);
            }
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEventId(event.getId());
        request.setRequesterId(user.getId());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest savedRequest = participationRequestRepository.save(request);
        return participationRequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.debug("Запрос на получение event клиентом из updateRequestStatus сервиса {}", serviceName);
        EventFullDto event = eventServiceClient.getEventFullDtoByIdClient(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Event does not require request moderation");
        }

        List<Long> requestIds = new ArrayList<>(updateRequest.getRequestIds());
        if (requestIds.isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        Map<Long, ParticipationRequest> requestsMap = participationRequestRepository.findByIdIn(requestIds)
                .stream()
                .collect(Collectors.toMap(ParticipationRequest::getId, Function.identity()));

        validateAllRequestsFound(requestIds, requestsMap);

        validateRequestsMap(requestsMap, eventId);

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        RequestStatus status = updateRequest.getStatus();
        if (status == RequestStatus.CONFIRMED) {
            processConfirmationWithMap(event, requestsMap, requestIds, confirmedRequests, rejectedRequests);
        } else if (status == RequestStatus.REJECTED) {
            processRejectionWithMap(requestsMap, requestIds, rejectedRequests);
        }

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        //Проверка на существование пользователя в бд
        log.debug("UserServiceClient validateUserExistingById received request cancelRequest from {}", serviceName);
        userServiceClient.validateUserExistingById(userId);

        ParticipationRequest request = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " not found"));

        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId +
                    " not found for user with id=" + userId);
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest savedRequest = participationRequestRepository.save(request);
        return participationRequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    public Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(
            List<Long> eventIds,
            RequestStatus requestStatus) {
        List<ParticipationRequest> requests = participationRequestRepository.findAllByEventIdInAndStatus(eventIds, requestStatus);
        return requests.stream().map(participationRequestMapper::toParticipationRequestDto).collect(Collectors.groupingBy(ParticipationRequestDto::getEvent));
    }

    private void validateAllRequestsFound(List<Long> requestIds, Map<Long, ParticipationRequest> requestsMap) {
        if (requestsMap.size() != requestIds.size()) {
            List<Long> foundIds = new ArrayList<>(requestsMap.keySet());
            List<Long> missingIds = requestIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new NotFoundException("Requests not found with ids: " + missingIds);
        }
    }

    private void validateRequestsMap(Map<Long, ParticipationRequest> requestsMap, Long eventId) {
        List<String> errors = new ArrayList<>();

        for (ParticipationRequest request : requestsMap.values()) {
            if (!request.getEventId().equals(eventId)) {
                errors.add("Request with id=" + request.getId() + " does not belong to event with id=" + eventId);
            }
            if (request.getStatus() != RequestStatus.PENDING) {
                errors.add("Request with id=" + request.getId() + " must be in PENDING state");
            }
        }

        if (!errors.isEmpty()) {
            throw new ConflictException(String.join("; ", errors));
        }
    }

    private void processConfirmationWithMap(EventFullDto event, Map<Long, ParticipationRequest> requestsMap,
                                            List<Long> requestIds, List<ParticipationRequestDto> confirmedRequests,
                                            List<ParticipationRequestDto> rejectedRequests) {
        Long confirmedCount = participationRequestRepository.countConfirmedRequestsByEventId(event.getId());
        int participantLimit = event.getParticipantLimit();
        int availableSlots = participantLimit - confirmedCount.intValue();

        List<ParticipationRequest> requestsToUpdate = new ArrayList<>();
        List<ParticipationRequest> requestsToReject = new ArrayList<>();

        for (int i = 0; i < requestIds.size(); i++) {
            Long requestId = requestIds.get(i);
            ParticipationRequest request = requestsMap.get(requestId);

            if (i < availableSlots) {
                request.setStatus(RequestStatus.CONFIRMED);
                requestsToUpdate.add(request);
            } else {
                request.setStatus(RequestStatus.REJECTED);
                requestsToReject.add(request);
            }
        }

        saveAndMapResults(requestsToUpdate, requestsToReject, confirmedRequests, rejectedRequests);

        if (availableSlots <= requestsToUpdate.size()) {
            rejectAllPendingRequests(event.getId(), rejectedRequests);
        }
    }

    private void processRejectionWithMap(Map<Long, ParticipationRequest> requestsMap,
                                         List<Long> requestIds, List<ParticipationRequestDto> rejectedRequests) {
        List<ParticipationRequest> requestsToReject = requestIds.stream()
                .map(requestsMap::get)
                .collect(Collectors.toList());

        requestsToReject.forEach(request -> request.setStatus(RequestStatus.REJECTED));
        List<ParticipationRequest> savedRequests = participationRequestRepository.saveAll(requestsToReject);

        rejectedRequests.addAll(savedRequests.stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .toList());
    }

    private void saveAndMapResults(List<ParticipationRequest> requestsToUpdate,
                                   List<ParticipationRequest> requestsToReject,
                                   List<ParticipationRequestDto> confirmedRequests,
                                   List<ParticipationRequestDto> rejectedRequests) {
        if (!requestsToUpdate.isEmpty()) {
            List<ParticipationRequest> savedConfirmed = participationRequestRepository.saveAll(requestsToUpdate);
            confirmedRequests.addAll(savedConfirmed.stream()
                    .map(participationRequestMapper::toParticipationRequestDto)
                    .toList());
        }

        if (!requestsToReject.isEmpty()) {
            List<ParticipationRequest> savedRejected = participationRequestRepository.saveAll(requestsToReject);
            rejectedRequests.addAll(savedRejected.stream()
                    .map(participationRequestMapper::toParticipationRequestDto)
                    .toList());
        }
    }

    private void rejectAllPendingRequests(Long eventId, List<ParticipationRequestDto> rejectedRequests) {
        List<ParticipationRequest> pendingRequests = participationRequestRepository
                .findByEventIdAndStatus(eventId, RequestStatus.PENDING);

        if (!pendingRequests.isEmpty()) {
            Map<Long, ParticipationRequest> pendingMap = pendingRequests.stream()
                    .collect(Collectors.toMap(ParticipationRequest::getId, Function.identity()));

            pendingMap.values().forEach(request -> request.setStatus(RequestStatus.REJECTED));
            List<ParticipationRequest> savedRequests = participationRequestRepository.saveAll(pendingMap.values());

            rejectedRequests.addAll(savedRequests.stream()
                    .map(participationRequestMapper::toParticipationRequestDto)
                    .toList());
        }
    }
}