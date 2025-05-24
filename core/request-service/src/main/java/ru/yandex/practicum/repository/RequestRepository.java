package ru.yandex.practicum.repository;

import ru.yandex.practicum.dto.requests.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByRequesterIdAndEventId(Long requestId, Long userId);

    @Query("SELECT COUNT (r) FROM Request r WHERE r.eventId = :eventId AND r.status = :status")
    Long countRequestsByEventAndStatus(Long eventId, RequestStatus status);

    List<Request> findByRequesterId(Long userId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    Optional<Request> findByEventId(Long eventId);

    Optional<Request> findByIdAndEventId(Long requestId, Long eventId);

    List<Request> findAllByEventIdInAndStatus(List<Long> eventIds, RequestStatus state);

}
