package ru.yandex.practicum.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionService {

    void collectUserAction(UserActionAvro userActionAvro);

}
