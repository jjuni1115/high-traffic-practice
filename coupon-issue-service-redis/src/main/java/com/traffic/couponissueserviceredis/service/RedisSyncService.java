package com.traffic.couponissueserviceredis.service;

import com.traffic.couponissueserviceredis.entity.CouponIssueEntity;
import com.traffic.couponissueserviceredis.entity.CouponMasterEntity;
import com.traffic.couponissueserviceredis.entity.RedisCouponMaster;
import com.traffic.couponissueserviceredis.repository.CouponIssueRepository;
import com.traffic.couponissueserviceredis.repository.CouponMasterRepository;
import com.traffic.couponissueserviceredis.repository.RedisCouponMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSyncService {

    private final StringRedisTemplate redisTemplate;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponMasterRepository couponMasterRepository;
    private final RedisCouponMasterRepository redisCouponMasterRepository;

    @Scheduled(cron = "0 0/1 * * * ?") // 10초마다 동기화
    public void syncToDb() {
        Iterable<RedisCouponMaster> allCoupons =
                redisCouponMasterRepository.findAll();

        for (RedisCouponMaster redisCoupon : allCoupons) {
            Long couponId = redisCoupon.getId();
            String syncKey = "coupon" + couponId + "sync";

            String userId;
            while ((userId = redisTemplate.opsForList().leftPop(syncKey)) != null) {
                log.info("Syncing couponId {} for userId {}", couponId, userId);

                CouponMasterEntity master = couponMasterRepository.findById(Long.valueOf(couponId))
                        .orElseThrow(() -> new RuntimeException("Coupon not found"));

                CouponIssueEntity issue = new CouponIssueEntity();
                issue.setCouponMaster(master);
                issue.setUserId(userId);
                couponIssueRepository.save(issue);
            }

            CouponMasterEntity master = couponMasterRepository.findById(Long.valueOf(couponId))
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));
            master.setAmount(redisCoupon.getAmount());
            couponMasterRepository.save(master);

        }
    }
}
