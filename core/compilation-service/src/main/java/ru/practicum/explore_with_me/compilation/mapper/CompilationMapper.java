package ru.practicum.explore_with_me.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore_with_me.compilation.entity.Compilation;
import ru.practicum.explore_with_me.interaction_api.model.compilation.dto.CompilationDto;
import ru.practicum.explore_with_me.interaction_api.model.compilation.dto.NewCompilationDto;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    Compilation toEntity(NewCompilationDto dto);

    @Mapping(target = "events", ignore = true)
    CompilationDto toDto(Compilation compilation);
}