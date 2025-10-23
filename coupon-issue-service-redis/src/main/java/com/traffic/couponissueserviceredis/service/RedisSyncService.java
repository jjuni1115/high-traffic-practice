package com.traffic.couponissueserviceredis.service;

import com.traffic.couponissueserviceredis.entity.CouponIssueEntity;
import com.traffic.couponissueserviceredis.entity.CouponMasterEntity;
import com.traffic.couponissueserviceredis.entity.RedisCouponMaster;
import com.traffic.couponissueserviceredis.repository.CouponIssueRepository;
import com.traffic.couponissueserviceredis.repository.CouponMasterRepository;
import com.traffic.couponissueserviceredis.repository.RedisCouponMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSyncService {

    private final StringRedisTemplate redisTemplate;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponMasterRepository couponMasterRepository;
    private final RedisCouponMasterRepository redisCouponMasterRepository;

    @Scheduled(fixedDelay = 100000) // 10초마다 동기화
    public void syncToDb() {
        Iterable<RedisCouponMaster> allCoupons =
                redisCouponMasterRepository.findAll();

        for (RedisCouponMaster redisCoupon : allCoupons) {
            Long couponId = redisCoupon.getId();
            String syncKey = "coupon:" + couponId + ":sync";

            String userId;
            while ((userId = redisTemplate.opsForList().leftPop(syncKey)) != null) {
                CouponMasterEntity master = couponMasterRepository.findById(Long.valueOf(couponId))
                        .orElseThrow(() -> new RuntimeException("Coupon not found"));

                CouponIssueEntity issue = new CouponIssueEntity();
                issue.setCouponMaster(master);
                issue.setUserId(userId);
                couponIssueRepository.save(issue);
            }

            // DB에도 남은 수량 반영
            CouponMasterEntity master = couponMasterRepository.findById(Long.valueOf(couponId))
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));
            master.setAmount(redisCoupon.getAmount());
            couponMasterRepository.save(master);

        }
    }
}
