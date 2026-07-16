package ru.practicum.model.compilation.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateCompilationRequest {

    private Set<Long> eventsId;

    private Boolean pinned;

    private String title;

}