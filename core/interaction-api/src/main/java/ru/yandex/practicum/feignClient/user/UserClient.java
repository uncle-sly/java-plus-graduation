package ru.yandex.practicum.feignClient.user;


import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/api/v1/user")
public interface UserClient {

    @GetMapping
    UserDto getUserById(@RequestParam Long id) throws FeignException;

    @GetMapping("/list")
    List<UserDto> getUsersList(@RequestParam List<Long> ids) throws FeignException;

}
