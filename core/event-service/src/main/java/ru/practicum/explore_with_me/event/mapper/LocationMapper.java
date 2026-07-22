package ru.practicum.explore_with_me.event.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explore_with_me.event.entity.Location;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.LocationDto;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toDto(Location location);

    Location toLocation(LocationDto locationDto);
}
