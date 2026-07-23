package ru.practicum.explore_with_me.request.service;

import ru.practicum.explore_with_me.interaction_api.model.request.RequestStatus;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestService {
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(List<Long> eventIds, RequestStatus requestStatus);

    ParticipationRequestDto getUserRequestByUserIdAndEventId(Long userId, Long eventId);
}