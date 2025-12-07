package com.traffic.couponissueservicekafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.couponissueservicekafka.dto.KafkaCounponDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponKafkaService {



    private final KafkaTemplate<String,KafkaCounponDto> kafkaTemplate;

    @Async
    public void sendCouponIssue(KafkaCounponDto kafkaCounponDto) {
        kafkaTemplate.send("coupon-issue",kafkaCounponDto);
        log.info("Kafka Coupon Issue Sent: user-id : {} coupon-id : {}", kafkaCounponDto.userId(), kafkaCounponDto.couponId());
    }


}
