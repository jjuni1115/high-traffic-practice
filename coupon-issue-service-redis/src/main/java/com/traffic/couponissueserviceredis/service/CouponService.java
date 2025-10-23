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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
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
    private final Lock lock = new ReentrantLock();

    @Transactional
    public String issueCoupon(Long couponId, String userId) {

/*        RedisCouponMaster redisCouponMaster = redisCouponMasterRepository.findById(couponId).orElseGet(() -> null);
        if(redisCouponMaster==null){
            return "Coupon does not exist";
        }*/


        String script = "local key = KEYS[1]\n" +
                "local userId = ARGV[1]\n" +
                "local issuedKey = ARGV[2]\n" +
                "local syncKey = ARGV[3]\n" +
                "\n" +
                "local amountStr = redis.call('HGET', key, 'amount')\n" +
                "if not amountStr then\n" +
                "  return -2  -- coupon not exists\n" +
                "end\n" +
                "\n" +
                "local amount = tonumber(amountStr)\n" +
                "if not amount then\n" +
                "  return -3  -- amount parse error\n" +
                "end\n" +
                "\n" +
                "if amount <= 0 then\n" +
                "  return -1  -- no stock\n" +
                "end\n" +
                "\n" +
                "-- 중복 발급 방지: 이미 발급된 사용자면 실패\n" +
                "if redis.call('SISMEMBER', issuedKey, userId) == 1 then\n" +
                "  return -4  -- already issued\n" +
                "end\n" +
                "\n" +
                "-- 재고 차감\n" +
                "redis.call('HINCRBY', key, 'amount', -1)\n" +
                "-- 발급 기록 추가 (Set, List 등)\n" +
                "redis.call('SADD', issuedKey, userId)\n" +
                "redis.call('RPUSH', syncKey, userId)\n" +
                "\n" +
                "return amount - 1  -- 남은 수량 반환";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        List<String> keys = Collections.singletonList("coupon_master:"+couponId);
        List<String> args = Arrays.asList(userId, "coupon" + couponId + "issued", "coupon" + couponId + "sync");

        Long result = redisTemplate.execute(redisScript, keys, args.toArray());

        if(result == null){
            return "server error";
        } else if (result == -2L){
            return "Coupon does not exist";
        } else if(result == -1L){
            return"No coupons left to issue";
        } else if (result == -4L){
            return "Coupon already issued to this user";
        }else if(result>0){
            return "Coupon issued!";
        }

/*        String issuedKey = "coupon"+String.valueOf(couponId) + "issued";
        if(redisTemplate.opsForSet().isMember(issuedKey,userId).equals(Boolean.TRUE)){
            return "Coupon already issued to this user";
        }

        if(redisCouponMaster.getAmount()<1){
            return"No coupons left to issue";
        }*/

        return "";

        /*redisCouponMaster.setAmount(redisCouponMaster.getAmount()-1);
        redisCouponMasterRepository.save(redisCouponMaster);

        redisTemplate.opsForSet().add(issuedKey,userId);
        redisTemplate.opsForList().rightPush("coupon"+String.valueOf(couponId)+"sync",userId);



        return "Coupon issued!";*/
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
