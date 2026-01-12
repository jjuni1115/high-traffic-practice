package com.traffic.couponissueservicekafka.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.traffic.couponissueservicekafka.dto.CouponRequestDto;
import com.traffic.couponissueservicekafka.dto.CouponResponseDto;
import com.traffic.couponissueservicekafka.dto.KafkaCounponDto;
import com.traffic.couponissueservicekafka.entity.CouponIssueEntity;
import com.traffic.couponissueservicekafka.entity.CouponMasterEntity;
import com.traffic.couponissueservicekafka.entity.RedisCouponMaster;
import com.traffic.couponissueservicekafka.repository.CouponIssueRepository;
import com.traffic.couponissueservicekafka.repository.CouponMasterRepository;
import com.traffic.couponissueservicekafka.repository.RedisCouponMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponMasterRepository couponMasterRepository;
    private final CouponIssueRepository couponIssueRepository;

    private final RedisCouponMasterRepository redisCouponMasterRepository;
    private final StringRedisTemplate redisTemplate;

    private final CouponKafkaService couponKafkaService;
    private final Lock lock = new ReentrantLock();

    public String issueCoupon(Long couponId, String userId) throws JsonProcessingException {


        String script = "local stock = tonumber(redis.call('GET', KEYS[1]))\n" +
                "if not stock or stock <= 0 then\n" +
                "  return -1\n" +
                "end\n" +
                "\n" +
                "-- 중복 방지\n" +
                "local ok = redis.call('SETNX', KEYS[2], 1)\n" +
                "if ok == 0 then\n" +
                "  return -2\n" +
                "end\n" +
                "\n" +
                "redis.call('DECR', KEYS[1])\n" +
                "return stock - 1";

        long start = System.currentTimeMillis();
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);


        String stockKey = "coupon:" + couponId + ":stock";
        String userKey = "coupon:" + couponId + ":user:" + userId;

        Long result = redisTemplate.execute(redisScript, List.of(stockKey, userKey));
        log.info("redis lua took = {}ms", System.currentTimeMillis() - start);

        if (result == null) return "server error";
        if (result == -1) return "No stock";
        if (result == -2) return "Already issued";

        //kafka로 발급 내역 전송
        KafkaCounponDto kafkaCounponDto = KafkaCounponDto.builder()
                .couponId(couponId)
                .userId(userId)
                .build();

        couponKafkaService.sendCouponIssue(kafkaCounponDto);
        return "Coupon issued!";



    }

    @Transactional
    public String createCoupon(CouponRequestDto couponRequestDto) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        CouponMasterEntity coupon = new CouponMasterEntity();
        coupon.setCouponName(couponRequestDto.couponName());
        coupon.setAmount(couponRequestDto.amount());
        LocalDate date = LocalDate.parse(couponRequestDto.expireDate(), formatter);
        coupon.setExpireDate(date.atStartOfDay());
        couponMasterRepository.save(coupon);


        //redis 저장
        RedisCouponMaster redisCouponMaster = new RedisCouponMaster();
        redisCouponMaster.setCouponName(couponRequestDto.couponName());
        redisCouponMaster.setAmount(couponRequestDto.amount());
        redisCouponMaster.setExpireDate(date.atStartOfDay());
        redisCouponMaster.setId(coupon.getId());

        redisCouponMasterRepository.save(redisCouponMaster);

        String stockKey = "coupon:" + coupon.getId() + ":stock";
        redisTemplate.opsForValue()
                .set(stockKey, String.valueOf(couponRequestDto.amount()));

        return "Coupon created!";
    }

    @Transactional(readOnly = true)
    public String getUserCoupon(Long couponId, String userId) {


        CouponIssueEntity couponIssueEntity = couponIssueRepository.findByCouponMaster_IdAndUserId(couponId, userId);

        if (couponIssueEntity == null) {

            return "no coupon for this user";

        }

        return "coupon : " + couponIssueEntity.getCouponMaster().getId() + " for user: " + couponIssueEntity.getUserId();


    }

    @Transactional
    public CouponResponseDto viewCoupon(Long couponId) {

        CouponMasterEntity couponMasterEntity = couponMasterRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        return new CouponResponseDto(
                couponMasterEntity.getId(),
                couponMasterEntity.getCouponName(),
                couponMasterEntity.getAmount(),
                formatter.format(couponMasterEntity.getExpireDate())
        );
    }

}
