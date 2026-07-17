package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.entity.Compilation;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.compilation.dto.NewCompilationDto;
import ru.practicum.model.compilation.dto.UpdateCompilationRequest;
import ru.practicum.model.event.client.EventServiceClient;
import ru.practicum.model.event.dto.EventShortDto;
import ru.practicum.repository.CompilationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventServiceClient eventServiceClient;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createNewCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание новой подборки: title={}, pinned={}, events={}",
                newCompilationDto.getTitle(),
                newCompilationDto.getPinned(),
                newCompilationDto.getEventsId());

        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            log.warn("Попытка создать подборку с уже существующим названием: {}", newCompilationDto.getTitle());
            throw new ConflictException("Подборка с названием '" + newCompilationDto.getTitle() + "' уже существует");
        }

        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.getEventsId() != null && !newCompilationDto.getEventsId().isEmpty()) {
            compilation.setEventsId(newCompilationDto.getEventsId());
        } else {
            compilation.setEventsId(new HashSet<>());
            log.debug("Подборка создаётся без событий");
        }

        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }

        log.debug("Сохранение подборки в БД");
        Compilation saved = compilationRepository.save(compilation);
        log.info("Подборка успешно создана с id: {}", saved.getId());
        return compilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        log.info("Запрос на удаление подборки с id: {}", compId);

        if (!compilationRepository.existsById(compId)) {
            log.warn("Подборка с id={} не найдена, удаление невозможно", compId);
            throw new NotFoundException("Подборка с id=" + compId + " не найдена");
        }

        log.debug("Подборка с id={} найдена, выполняется удаление", compId);
        compilationRepository.deleteById(compId);
        log.info("Подборка с id={} успешно удалена", compId);
    }

    @Override
    @Transactional
    public CompilationDto patchCompilation(long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Запрос на обновление подборки по id: {}", compId);
        Compilation compilation = getCompilation(compId);

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        if (updateCompilationRequest.getTitle() != null) {
            if (!updateCompilationRequest.getTitle().equals(compilation.getTitle())) {
                if (compilationRepository.existsByTitle(updateCompilationRequest.getTitle())) {
                    log.warn("Попытка изменить название на уже существующее: {}", updateCompilationRequest.getTitle());
                    throw new ConflictException("Подборка с названием '" + updateCompilationRequest.getTitle() + "' уже существует");
                }
                compilation.setTitle(updateCompilationRequest.getTitle());
            }
        }

        if (updateCompilationRequest.getEventsId() != null) {
            compilation.setEventsId(new HashSet<>(updateCompilationRequest.getEventsId()));
        }

        compilationRepository.save(compilation);
        return compilationMapper.toDto(compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(long compId) {
        log.info("Запрос на получение подборки по id: {}", compId);
        Compilation compilation = getCompilation(compId);
        log.debug("Подборка с id={} найдена: {}", compId, compilation);
        return compilationMapper.toDto(compilation);
    }


    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Запрос на получение подборок: pinned={}, from={}, size={}", pinned, from, size);

        List<Long> ids = compilationRepository.findCompilationIds(pinned, from, size);
        log.debug("Найдено ID подборок: {}", ids);

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Compilation> compilations = compilationRepository.findCompilationsWithEventsByIds(ids);
        log.debug("Загружено подборок с событиями: {}", compilations.size());

        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Подборка с id={} не найдена", compId);
                    return new NotFoundException("Подборка с id=" + compId + " не найдена");
                });
    }

    private Set<EventShortDto> getEventList(Set<Long> eventIds) {
        log.debug("Загрузка событий по списку id: {}", eventIds);
        Set<EventShortDto> events = eventServiceClient.getEventShortDtoSetByIds(eventIds);

        if (events.size() != eventIds.size()) {
            Set<Long> foundIds = events.stream()
                    .map(EventShortDto::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(eventIds);
            missingIds.removeAll(foundIds);
            log.warn("Не найдены события с id: {}", missingIds);
            throw new NotFoundException("События с id " + missingIds + " не найдены");
        }
        log.debug("В подборку добавлено {} событий", events.size());
        return events;
    }
}