package ru.yandex.practicum.mapper;


import org.mapstruct.Mapper;
import ru.yandex.practicum.dto.UserShortDto;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    UserShortDto toUserShortDto(User user);

    List<UserDto> toUserDtoList(List<User> users);

    User toUser(UserDto userDto);
}