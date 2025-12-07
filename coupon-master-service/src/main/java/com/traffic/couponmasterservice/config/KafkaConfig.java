package com.traffic.couponmasterservice.config;

import com.traffic.couponmasterservice.dto.KafkaCounponDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConfig {


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaCounponDto> kafkaListenerContainerFactory(
            ConsumerFactory<String, KafkaCounponDto> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, KafkaCounponDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, KafkaCounponDto> consumerFactory() {
        JsonDeserializer<KafkaCounponDto> deserializer = new JsonDeserializer<>(KafkaCounponDto.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(false);  // 중요
        deserializer.setRemoveTypeHeaders(true);

        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
                ConsumerConfig.GROUP_ID_CONFIG, "coupon-master",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
        ), new StringDeserializer(), deserializer);
    }

}
