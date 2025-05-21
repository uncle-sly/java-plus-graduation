package ewm.event.dto;

import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.user.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventBaseDto {
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private Long id;
    private UserDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
    private Long commentsCount;
}
