package ru.yandex.practicum.feignClient;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.user.UserDto;

import java.util.List;

public interface UserOperationsClient {

    @GetMapping
    UserDto getUserById(@RequestParam Long id) throws FeignException;

    @GetMapping("/list")
    List<UserDto> getUsersList(@RequestParam List<Long> ids) throws FeignException;

}
