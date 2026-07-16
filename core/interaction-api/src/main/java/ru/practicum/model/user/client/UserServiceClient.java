package ru.practicum.model.user.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

@FeignClient(
        name = "user-service",
        path = "/admin/users"
)
public interface UserServiceClient {

    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable @Positive Long id);

    @GetMapping("/{userId}")
    UserShortDto getUserShortById(@PathVariable @Positive Long id);

    @GetMapping("/client/exist/{userId}")
    void validateUserExistingById(@PathVariable Long userId);
}
