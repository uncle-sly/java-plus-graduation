package ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.dto.event.EventBaseDto;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class EventShortDto extends EventBaseDto {
    private String eventDate;
}
