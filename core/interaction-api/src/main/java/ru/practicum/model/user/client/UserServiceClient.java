package ru.practicum.model.user.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.user.dto.UserDto;

@FeignClient(
        name = "user-service",
        path = "/admin/users"
)
public interface UserServiceClient {

    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable @Positive Long id);
}
