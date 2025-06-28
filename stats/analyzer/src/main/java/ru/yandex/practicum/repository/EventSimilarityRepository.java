package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.EventSimilarity;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    List<EventSimilarity> findByEventAOrEventB(long eventId, long eventId1);

    List<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);

}
