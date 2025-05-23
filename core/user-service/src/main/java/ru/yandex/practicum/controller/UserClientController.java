package ru.yandex.practicum.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.feignClient.UserOperationsClient;
import ru.yandex.practicum.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Slf4j
public class UserClientController implements UserOperationsClient {

    private final UserService userService;

    @Override
    public UserDto getUserById(Long id) throws FeignException {
        log.info("get user by id = {}", id);
        return userService.getUserById(id);
    }

    @Override
    public List<UserDto> getUsersList(List<Long> ids) throws FeignException {
        log.info("get users by ids = {}", ids);
        return userService.getUsersList(ids);
    }

}
