package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    @Mapping(target = "id", ignore = true)
    Compilation toEntity(NewCompilationDto dto);

    CompilationDto toDto(Compilation compilation);
}