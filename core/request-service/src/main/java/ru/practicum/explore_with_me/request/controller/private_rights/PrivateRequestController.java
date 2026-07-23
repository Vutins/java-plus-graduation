package ru.practicum.explore_with_me.request.controller.private_rights;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.interaction_api.model.request.RequestStatus;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;
import ru.practicum.explore_with_me.request.service.RequestService;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final RequestService participationRequestService;

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long eventId) {
        return participationRequestService.getEventRequests(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        return participationRequestService.getUserRequests(userId);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam @Positive Long eventId) {
        return participationRequestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable @Positive Long userId,
                                                              @PathVariable @Positive Long eventId,
                                                              @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        return participationRequestService.updateRequestStatus(userId, eventId, updateRequest);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return participationRequestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/client/count")
    public Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(
            @RequestParam List<Long> eventIds,
            @RequestParam RequestStatus requestStatus) {
        return participationRequestService.getConfirmedRequestsCount(eventIds, requestStatus);
    }

    @GetMapping("/{userId}/client/event/{eventId}")
    public ParticipationRequestDto getUserRequestByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {
        return participationRequestService.getUserRequestByUserIdAndEventId(userId, eventId);
    }
}