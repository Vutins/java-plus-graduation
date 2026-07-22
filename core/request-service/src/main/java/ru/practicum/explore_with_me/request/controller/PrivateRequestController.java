package ru.practicum.explore_with_me.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;
import ru.practicum.explore_with_me.interaction_api.model.request.enums.RequestStatus;
import ru.practicum.explore_with_me.request.service.RequestService;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping("/{userId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(
            @PathVariable @Min(1) Long userId) {
        return ResponseEntity.ok(requestService.getUserRequests(userId));
    }

    @PostMapping("/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(
            @PathVariable @Min(1) Long userId,
            @RequestParam(required = false) @Min(1) Long eventId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(requestService.addParticipationRequest(userId, eventId));
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable @Min(1) Long userId,
            @PathVariable @Min(1) Long requestId) {
        return ResponseEntity.ok(requestService.cancelRequest(userId, requestId));
    }

    @GetMapping("/client/count")
    public Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(
            @RequestParam List<Long> eventIds,
            @RequestParam RequestStatus requestStatus) {
        return requestService.getConfirmedRequestsCount(eventIds, requestStatus);
    }

    @GetMapping("/{userId}/client/event/{eventId}")
    public ParticipationRequestDto getUserRequestByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {
        return requestService.getUserRequestByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("{userId}/client/event/{eventId}")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody @Valid @NotNull EventRequestStatusUpdateRequest request) {
        return requestService.changeRequestStatus(userId, eventId, request);
    }

    @GetMapping("{userId}/client/list/requests/event/{eventId}")
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getEventParticipants(userId, eventId);
    }

    @GetMapping("/internal/events/{eventId}/count")
    public Long getConfirmedRequestsCountByEventId(@PathVariable Long eventId) {
        return requestService.getConfirmedRequestsCountByEventId(eventId);
    }

    @GetMapping("/internal/events/count")
    public List<Object[]> countConfirmedRequestsForEvents(@RequestBody List<Long> events) {
        return requestService.countConfirmedRequestsForEvents(events);
    }
}