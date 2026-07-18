package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.model.user.dto.UserDto;
import ru.practicum.model.user.dto.UserRequestDto;
import ru.practicum.model.user.dto.UserShortDto;

import java.util.List;

public interface UserAdminService {

    UserDto create(UserRequestDto userRequestDto);

    List<UserDto> getAllUsers(List<Long> ids, Pageable pageable);

    UserDto getUserById(Long id);

    void deleteUser(Long id);

    UserShortDto getUserShortById (Long id);

    void validateUserExistingById(Long userId);

}
