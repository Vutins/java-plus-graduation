package ru.practicum.explore_with_me.interaction_api.model.request.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explore_with_me.interaction_api.model.request.RequestStatus;
import ru.practicum.explore_with_me.interaction_api.model.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "request-service",
        path = "/users"
)
public interface ParticipationRequestServiceClient {

    @GetMapping("/client/count")
    Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(
            @RequestParam("eventIds") List<Long> eventIds,
            @RequestParam("requestStatus") RequestStatus requestStatus);

    @GetMapping("/{userId}/client/event/{eventId}")
    ParticipationRequestDto getUserRequestByUserIdAndEventId(
            @PathVariable("userId") @Positive Long userId,
            @PathVariable("eventId") @Positive Long eventId);
}