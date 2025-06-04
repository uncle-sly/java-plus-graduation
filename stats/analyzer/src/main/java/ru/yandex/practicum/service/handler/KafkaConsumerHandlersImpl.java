package ru.yandex.practicum.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.mapper.EventSimilarityMapper;
import ru.yandex.practicum.mapper.UserActionMapper;
import ru.yandex.practicum.model.EventSimilarity;
import ru.yandex.practicum.model.UserAction;
import ru.yandex.practicum.repository.EventSimilarityRepository;
import ru.yandex.practicum.repository.UserActionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerHandlersImpl implements KafkaConsumerHandlers {

    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Override
    public void eventsSimilarityGot(EventSimilarityAvro eventSimilarityAvro) {
        log.info("получили сообщение о сходстве: {}", eventSimilarityAvro);
        EventSimilarity eventSimilarity = EventSimilarityMapper.mapToEventSimilarity(eventSimilarityAvro);

        List<EventSimilarity> similarities =
                eventSimilarityRepository.findByEventAAndEventB(eventSimilarity.getEventA(), eventSimilarity.getEventB());
        if (!similarities.isEmpty()) {
            EventSimilarity oldSimilarity = similarities.getFirst();
            oldSimilarity.setScore(eventSimilarity.getScore());
            oldSimilarity.setTimestamp(eventSimilarity.getTimestamp());
            eventSimilarityRepository.save(oldSimilarity);
            log.info("обновили сходство: {}", oldSimilarity);
        } else {
            eventSimilarityRepository.save(eventSimilarity);
            log.info("сохранили новое сходство: {}", eventSimilarity);
        }
    }

    @Override
    public void userActionGot(UserActionAvro userActionAvro) {
        log.info("получили сообщение о действиях пользователя: {}", userActionAvro);
        UserAction userAction = UserActionMapper.mapToUserAction(userActionAvro);

        List<UserAction> actions =
                userActionRepository.findByUserIdAndEventId(userActionAvro.getUserId(), userActionAvro.getEventId());
        if (!actions.isEmpty()) {
            UserAction oldAction = actions.getFirst();
            if (oldAction.getActionType().getScore() < userAction.getActionType().getScore()) {
                oldAction.setActionType(userAction.getActionType());
                oldAction.setTimestamp(userAction.getTimestamp());
                userActionRepository.save(oldAction);
                log.info("обновили действия пользователя: {}", oldAction);
            }
        } else {
            userActionRepository.save(userAction);
            log.info("сохранили действия пользователя: {}", userAction);
        }
    }

}