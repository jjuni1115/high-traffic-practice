package com.traffic.couponissueservice.service;

import com.traffic.couponissueservice.entity.CouponIssueEntity;
import com.traffic.couponissueservice.entity.CouponMasterEntity;
import com.traffic.couponissueservice.repository.CouponIssueRepository;
import com.traffic.couponissueservice.repository.CouponMasterRepository;
import dto.CouponRequestDto;
import lombok.RequiredArgsConstructor;
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
    private final Lock lock = new ReentrantLock();

    @Transactional
    public String issueCoupon(Long couponId, String userId) {

        lock.lock();
        try {
            CouponMasterEntity coupon = couponMasterRepository.findById(couponId).orElseThrow(() -> new RuntimeException("Coupon not found"));

            if (coupon.getAmount() < 1) {
                throw new RuntimeException("No coupons left");
            }

            if(couponIssueRepository.countByCouponMasterAndUserId(coupon,userId)>0){
                throw new RuntimeException("User has already issued this coupon");
            }

            coupon.setAmount(coupon.getAmount() - 1);

            CouponIssueEntity couponIssue = new CouponIssueEntity();
            couponIssue.setUserId(userId);
            couponIssue.setCouponMaster(coupon);
            couponIssueRepository.save(couponIssue);

        } finally {
            lock.unlock();

        }


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
        return "Coupon created!";
    }

}
