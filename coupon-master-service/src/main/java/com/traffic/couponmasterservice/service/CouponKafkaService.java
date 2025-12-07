package com.traffic.couponmasterservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.couponmasterservice.dto.KafkaCounponDto;
import com.traffic.couponmasterservice.entity.CouponIssueEntity;
import com.traffic.couponmasterservice.entity.CouponMasterEntity;
import com.traffic.couponmasterservice.repository.CouponIssueRepository;
import com.traffic.couponmasterservice.repository.CouponMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponKafkaService {

    private final CouponMasterRepository couponMasterRepository;
    private final CouponIssueRepository couponIssueRepository;


    private final KafkaTemplate<String,String> kafkaTemplate;

    @KafkaListener(topics = "coupon-issue",groupId = "coupon-master",containerFactory = "kafkaListenerContainerFactory")
    public void listenCouponIssue(KafkaCounponDto kafkaCounponDto) {
        log.info("Kafka Coupon Issue Received: user-id : {} coupon-id : {}", kafkaCounponDto.userId(), kafkaCounponDto.couponId());

        CouponMasterEntity coupon = couponMasterRepository.findById(kafkaCounponDto.couponId()).orElseThrow(() -> new RuntimeException("Coupon not found"));

        coupon.setAmount(coupon.getAmount() - 1);

        CouponIssueEntity couponIssue = new CouponIssueEntity();
        couponIssue.setUserId(kafkaCounponDto.userId());
        couponIssue.setCouponMaster(coupon);
        couponIssueRepository.save(couponIssue);

        couponMasterRepository.save(coupon);

    }


}
