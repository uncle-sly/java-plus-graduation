package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "u_actions")
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    private UserActionType actionType;

    @Column(name = "timestamp_ts")
    private LocalDateTime timestamp;

}