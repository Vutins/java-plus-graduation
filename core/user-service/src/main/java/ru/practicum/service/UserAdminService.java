package ru.practicum.service;

import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserRequestDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

public interface UserAdminService {

    UserDto create(UserRequestDto userRequestDto);

    List<UserDto> getAllUsers(List<Long> ids, Pageable pageable);

    UserDto getUserById(Long id);

    void deleteUser(Long id);

    UserShortDto getUserShortById (Long id);

    void validateUserExistingById(Long userId);

}
