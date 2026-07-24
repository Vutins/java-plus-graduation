package ru.practicum.explore_with_me.user.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.NewUserRequest;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserDto;
import ru.practicum.explore_with_me.interaction_api.model.user.dto.UserShortDto;
import ru.practicum.explore_with_me.user.service.UserService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        return userService.getUsers(ids, PageRequest.of(from / size, size));
    }

    @GetMapping("/client/exist/{userId}")
    public void validateUserExistingById(@PathVariable Long userId) {
        userService.validateUserExistingById(userId);
    }

    @GetMapping("/client/{userId}")
    public UserShortDto getUserShortDtoClientById(@PathVariable Long userId) {
        return userService.getUserShortDtoClientById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        return userService.createUser(newUserRequest);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/client/list")
    public List<UserShortDto> getUsersShortByIds(@RequestParam List<Long> ids) {
        return userService.getUsersShortByIds(ids);
    }
}