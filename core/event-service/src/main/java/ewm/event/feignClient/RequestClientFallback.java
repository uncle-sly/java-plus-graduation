package ewm.event.feignClient;

import ewm.exception.EntityNotFoundException;
import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.dto.requests.RequestStatus;

import java.util.List;
import java.util.Optional;

@Component
public class RequestClientFallback implements RequestClient {

    @Override
    public Long countRequestsByEventAndStatus(Long eventId, RequestStatus status) throws FeignException {
        return 0L;
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> ids, RequestStatus status) throws FeignException {
        return List.of();
    }

    @Override
    public Optional<ParticipationRequestDto> findByRequestIdAndEventId(Long requestId, Long eventId) throws FeignException {
        throw new EntityNotFoundException(RequestClientFallback.class,
                "Запрос с ID: %d, на Событие с ID: %d не обнаружен.".formatted(requestId, eventId));
    }

}
