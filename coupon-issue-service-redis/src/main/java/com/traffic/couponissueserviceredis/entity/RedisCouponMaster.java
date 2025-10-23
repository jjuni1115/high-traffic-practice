package com.traffic.couponissueserviceredis.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;

@RedisHash("coupon_master")
@Getter
@Setter
public class RedisCouponMaster {
    @Id
    private Long id;
    private String couponName;
    private Long amount;
    private LocalDateTime expireDate;

}
