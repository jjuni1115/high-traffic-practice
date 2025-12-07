package com.traffic.couponmasterservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.traffic.couponmasterservice.dto.CouponRequestDto;
import com.traffic.couponmasterservice.dto.CouponResponseDto;
import com.traffic.couponmasterservice.dto.KafkaCounponDto;
import com.traffic.couponmasterservice.entity.CouponIssueEntity;
import com.traffic.couponmasterservice.entity.CouponMasterEntity;
import com.traffic.couponmasterservice.entity.RedisCouponMaster;
import com.traffic.couponmasterservice.repository.CouponIssueRepository;
import com.traffic.couponmasterservice.repository.CouponMasterRepository;
import com.traffic.couponmasterservice.repository.RedisCouponMasterRepository;
import lombok.RequiredArgsConstructor;
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
public class CouponService {

    private final CouponMasterRepository couponMasterRepository;
    private final CouponIssueRepository couponIssueRepository;

    private final RedisCouponMasterRepository redisCouponMasterRepository;
    private final StringRedisTemplate redisTemplate;

    private final CouponKafkaService couponKafkaService;
    private final Lock lock = new ReentrantLock();



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

        return "Coupon created!";
    }

    @Transactional(readOnly = true)
    public String getUserCoupon(Long couponId, String userId){


        CouponIssueEntity couponIssueEntity = couponIssueRepository.findByCouponMaster_IdAndUserId(couponId,userId);

        if(couponIssueEntity==null){

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
