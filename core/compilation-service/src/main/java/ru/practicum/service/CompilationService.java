package ru.practicum.service;

import java.util.List;

public interface CompilationService {
    CompilationDto createNewCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(long compId);

    CompilationDto patchCompilation(long compId,UpdateCompilationRequest updateCompilationRequest);

    CompilationDto getCompilationById(long compId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);
}