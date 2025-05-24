package ewm.event.feignClient;

import ewm.exception.EntityNotFoundException;
import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.user.UserDto;

import java.util.List;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDto getUserById(Long id) throws FeignException {
        throw new EntityNotFoundException(UserClientFallback.class,
                "Пользователь с ID: " + id + " не обнаружен.");
    }

    @Override
    public List<UserDto> getUsersList(List<Long> ids) throws FeignException {
        return List.of();
    }

}
