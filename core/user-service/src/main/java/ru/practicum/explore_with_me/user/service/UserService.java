package ru.practicum.explore_with_me.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.NewUserRequest;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserDto;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;

import java.util.List;

public interface UserService {
    UserShortDto getUserShortDtoClientById(Long userId);

    void validateUserExistingById(Long userId);

    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

    List<UserShortDto> getUsersShortByIds(List<Long> ids);
}