package ru.practicum.model.compilation.dto;

import lombok.Data;
import ru.practicum.model.event.dto.EventShortDto;

import java.util.List;

@Data
public class CompilationDto {

    private List<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}