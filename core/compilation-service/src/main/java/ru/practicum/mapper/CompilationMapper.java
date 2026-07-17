package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.entity.Compilation;
import ru.practicum.model.compilation.dto.CompilationDto;
import ru.practicum.model.compilation.dto.NewCompilationDto;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    Compilation toEntity(NewCompilationDto dto);

    @Mapping(target = "events", ignore = true)
    CompilationDto toDto(Compilation compilation);
}