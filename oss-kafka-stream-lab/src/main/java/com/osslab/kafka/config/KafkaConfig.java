package com.osslab.kafka.config;

import com.osslab.kafka.dto.AlertNotificationDto;
import com.osslab.kafka.dto.OrderDto;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafkaStreams
public class KafkaConfig {

  @Bean
  public ProducerFactory<String, OrderDto> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, OrderDto> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public ConsumerFactory<String, OrderDto> consumerFactory() {
      Map<String, Object> props = new HashMap<>();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonSerializer.class);
      return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
  public KafkaStreamsConfiguration kafkaStreamsConfiguration() {
    Map<String, Object> props = new HashMap<>();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("application.id", "kafka-streams-app");
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
//    props.put("default.key.serde", "org.apache.kafka.common.serialization.Serdes$StringSerde");
//    props.put("default.value.serde", "org.apache.kafka.common.serialization.Serdes$JsonSerde");
    return new KafkaStreamsConfiguration(props);
  }


  @Bean
  public ConsumerFactory<String, AlertNotificationDto> alertConsumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");

    // 타입 헤더가 없거나 깨져도 안전하게 처리
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AlertNotificationDto.class.getName());
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    return new DefaultKafkaConsumerFactory<>(
        props,
        new StringDeserializer(),
        new JsonDeserializer<>(AlertNotificationDto.class, false) // removeTypeHeaders=false면 header 우선
    );
  }

  @Bean(name = "alertKafkaListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, AlertNotificationDto> alertKafkaListenerContainerFactory() {
    var f = new ConcurrentKafkaListenerContainerFactory<String, AlertNotificationDto>();
    f.setConsumerFactory(alertConsumerFactory());
    return f;
  }

}
