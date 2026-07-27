package ru.practicum.explore_with_me.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore_with_me.event.dao.Event;
import ru.practicum.explore_with_me.event.dao.Location;
import ru.practicum.explore_with_me.interaction_api.model.category.dto.CategoryDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventFullDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.EventShortDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.LocationDto;
import ru.practicum.explore_with_me.interaction_api.model.event.dto.NewEventDto;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "views", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    default Location toLocation(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }

    default LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());
        return locationDto;
    }

    default EventFullDto toEventFullDto(
            Event event,
            CategoryDto categoryDto,
            UserShortDto userShortDto
    ) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setTitle(event.getTitle());

        dto.setState(event.getState() != null ? event.getState().toString() : null);

        dto.setCategory(categoryDto);
        dto.setInitiator(userShortDto);

        if (event.getLocation() != null) {
            dto.setLocation(toLocationDto(event.getLocation()));
        }

        return dto;
    }

    default EventShortDto toEventShortDto(Event event, CategoryDto categoryDto, UserShortDto userShortDto) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setCategory(categoryDto);
        dto.setInitiator(userShortDto);
        dto.setAnnotation(event.getAnnotation());
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());

        return dto;
    }
}