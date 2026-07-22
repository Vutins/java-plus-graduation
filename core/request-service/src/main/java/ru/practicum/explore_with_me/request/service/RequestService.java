package ru.practicum.explore_with_me.request.service;

import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;
import ru.practicum.explore_with_me.interaction_api.model.request.enums.RequestStatus;

import java.util.List;
import java.util.Map;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(List<Long> eventIds, RequestStatus requestStatus);

    ParticipationRequestDto getUserRequestByUserIdAndEventId(Long userId, Long eventId);

    Long getConfirmedRequestsCountByEventId(Long eventId);

    List<Object[]> countConfirmedRequestsForEvents(List<Long> events);
}