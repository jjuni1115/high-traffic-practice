package com.traffic.couponissueserviceredis.service;


import com.traffic.couponissueserviceredis.dto.CouponRequestDto;
import com.traffic.couponissueserviceredis.entity.CouponIssueEntity;
import com.traffic.couponissueserviceredis.entity.CouponMasterEntity;
import com.traffic.couponissueserviceredis.entity.RedisCouponMaster;
import com.traffic.couponissueserviceredis.repository.CouponIssueRepository;
import com.traffic.couponissueserviceredis.repository.CouponMasterRepository;
import com.traffic.couponissueserviceredis.repository.RedisCouponMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMasterRepository couponMasterRepository;
    private final CouponIssueRepository couponIssueRepository;

    private final RedisCouponMasterRepository redisCouponMasterRepository;
    private final StringRedisTemplate redisTemplate;
    private final Lock lock = new ReentrantLock();

    @Transactional
    public String issueCoupon(Long couponId, String userId) {

        RedisCouponMaster redisCouponMaster = redisCouponMasterRepository.findById(couponId).orElseGet(() -> null);
        if(redisCouponMaster==null){
            return "Coupon does not exist";
        }
        String issuedKey = "coupon"+String.valueOf(couponId) + "issued";
        if(redisTemplate.opsForSet().isMember(issuedKey,userId).equals(Boolean.TRUE)){
            return "Coupon already issued to this user";
        }

        if(redisCouponMaster.getAmount()<1){
            return"No coupons left to issue";
        }

        redisCouponMaster.setAmount(redisCouponMaster.getAmount()-1);
        redisCouponMasterRepository.save(redisCouponMaster);

        redisTemplate.opsForSet().add(issuedKey,userId);
        redisTemplate.opsForList().rightPush("coupon"+String.valueOf(couponId)+"sync",userId);



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

}
