package ru.practicum.explore_with_me.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore_with_me.EndpointHitDto;
import ru.practicum.explore_with_me.StatResponseDto;
import ru.practicum.explore_with_me.model.dao.Stat;

@Mapper(componentModel = "spring")
public interface StatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "timestamp", target = "created")
    Stat toStat(EndpointHitDto endpointHitDto);

    StatResponseDto toStatResponseDto(Stat stat);
}