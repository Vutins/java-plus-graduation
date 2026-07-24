package ru.practicum.explore_with_me.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.compilation.dao.Compilation;
import ru.practicum.explore_with_me.compilation.mapper.CompilationMapper;
import ru.practicum.explore_with_me.compilation.repository.CompilationRepository;
import ru.practicum.explore_with_me.interaction_api.exception.NotFoundException;
import ru.practicum.explore_with_me.interaction_api.model.compilation.dto.CompilationDto;
import ru.practicum.explore_with_me.interaction_api.model.compilation.dto.NewCompilationDto;
import ru.practicum.explore_with_me.interaction_api.model.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explore_with_me.interaction_api.model.event.client.EventServiceClient;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventServiceClient eventServiceClient;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        if (compilations.isEmpty()) {
            return List.of();
        }

        Set<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEventsId().stream())
                .collect(Collectors.toSet());

        Map<Long, EventShortDto> eventMap = allEventIds.isEmpty()
                ? Map.of()
                : eventServiceClient.getEventShortDtoSetByIds(allEventIds).stream()
                .collect(Collectors.toMap(EventShortDto::getId, Function.identity()));

        return compilations.stream()
                .map(compilation -> {
                    Set<EventShortDto> events = compilation.getEventsId().stream()
                            .map(eventMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    return compilationMapper.toCompilationDto(compilation, events);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));
        Set<EventShortDto> events = eventServiceClient.getEventShortDtoSetByIds(compilation.getEventsId());
        return compilationMapper.toCompilationDto(compilation, events);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        Set<EventShortDto> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventServiceClient.getEventShortDtoSetByIds(newCompilationDto.getEvents());
            if (events.size() != newCompilationDto.getEvents().size()) {
                throw new NotFoundException("Некоторые события не найдены");
            }
            compilation.setEventsId(newCompilationDto.getEvents());
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(savedCompilation, events);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));
        Set<EventShortDto> events;

        if (updateRequest.getEvents() != null) {
            if (!updateRequest.getEvents().isEmpty()) {
                events = eventServiceClient.getEventShortDtoSetByIds(updateRequest.getEvents());
                if (events.size() != updateRequest.getEvents().size()) {
                    throw new NotFoundException("Некоторые события не найдены");
                }
            } else {
                events = Set.of();
            }
            compilation.setEventsId(updateRequest.getEvents());
        } else {
            events = eventServiceClient.getEventShortDtoSetByIds(compilation.getEventsId());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);

        return compilationMapper.toCompilationDto(updatedCompilation, events);
    }
}