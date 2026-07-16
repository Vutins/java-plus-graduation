package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.entity.Location;
import ru.practicum.entity.LocationDto;

@Component
public interface LocationMapper {

    LocationDto toDto(Location location);

    Location toLocation(LocationDto locationDto);
}
