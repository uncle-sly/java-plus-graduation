package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.UserAction;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    List<UserAction> findByUserId(Long userId);

    List<UserAction> findByEventId(Long eventId);

    List<UserAction> findByUserIdAndEventId(Long userId, Long eventId);

}
