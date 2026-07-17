package ru.practicum.model.request.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dto.ParticipationRequestDto;
import ru.practicum.model.request.enums.RequestStatus;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "request-service",
        path = "/users"
)
public interface RequestServiceClient {

    @GetMapping("/client/count")
    Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(
            @RequestParam("eventIds") List<Long> eventIds,
            @RequestParam("requestStatus") RequestStatus requestStatus);

    @GetMapping("/{userId}/client/event/{eventId}")
    ParticipationRequestDto getUserRequestByUserIdAndEventId(
            @PathVariable("userId") @Positive Long userId,
            @PathVariable("eventId") @Positive Long eventId);

    @PatchMapping("{userId}/client/event/{eventId}")
    EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody @Valid @NotNull EventRequestStatusUpdateRequest request);

    @GetMapping("{userId}/client/list/requests/event/{eventId}")
    List<ParticipationRequestDto> getEventParticipants(@PathVariable Long userId, @PathVariable Long eventId);
}
