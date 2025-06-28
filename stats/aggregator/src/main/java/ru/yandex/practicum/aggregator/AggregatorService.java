package ru.yandex.practicum.aggregator;

import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;

public interface AggregatorService {

    List<EventSimilarityAvro> processUserActions(SpecificRecordBase record);

}
