package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(UserDto userDto);

    void delete(Long id);

    UserDto getUserById(Long id);

    List<UserDto> getUsersList(List<Long> ids);

}
