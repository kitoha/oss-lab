package com.osslab.kafka.streams;

import com.osslab.kafka.config.KafkaTopicConfig;
import com.osslab.kafka.dto.AlertNotificationDto;
import com.osslab.kafka.dto.OrderDto;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WindowedAggregationProcessor {

  @Autowired
  public void process(StreamsBuilder streamsBuilder) {
    JsonSerde<OrderDto> jsonSerde = new JsonSerde<>(OrderDto.class);

    KStream<String, OrderDto> eventStream = streamsBuilder
        .stream(KafkaTopicConfig.INPUT_TOPIC, Consumed.with(Serdes.String(), jsonSerde));

    KTable<Windowed<String>, Long> aggregatedTable = eventStream
        .groupByKey(Grouped.with(Serdes.String(), jsonSerde))
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
        .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("1-min-event-counts")
            .withKeySerde(Serdes.String())
            .withValueSerde(Serdes.Long())
        ).suppress(Suppressed.untilWindowCloses(
        Suppressed.BufferConfig.unbounded()
    ));



    KStream<String, AlertNotificationDto> notificationDtoKStream = aggregatedTable
        .toStream()
        .map((readOnlyKey, value) -> {
          String key = readOnlyKey.key();
          long count = value == null ? 0L : value;
          long windowStart = readOnlyKey.window().start();
          long windowEnd = readOnlyKey.window().end();
          return KeyValue.pair(key, new AlertNotificationDto(key, count, windowStart, windowEnd));
        });

    notificationDtoKStream.to(
        KafkaTopicConfig.ALERTS_TOPIC,
        Produced.with(Serdes.String(), new JsonSerde<>(AlertNotificationDto.class))
    );
  }

}
