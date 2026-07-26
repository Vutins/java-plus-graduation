package ru.practicum.explore_with_me.service;

import ru.practicum.explore_with_me.EndpointHitDto;
import ru.practicum.explore_with_me.StatResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    void addHit(EndpointHitDto endpointHitDto);

    List<StatResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}