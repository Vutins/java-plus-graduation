package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.entity.Location;
import ru.practicum.model.event.dto.LocationDto;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toDto(Location location);

    Location toLocation(LocationDto locationDto);
}
