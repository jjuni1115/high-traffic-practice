package com.traffic.couponissueserviceredis.endpoint;


import com.traffic.couponissueserviceredis.dto.CouponRequestDto;
import com.traffic.couponissueserviceredis.service.CouponService;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponEndpoint {

    private final CouponService couponService;


    @PostMapping("/issue/{couponId}/{userId}")
    public ResponseEntity<String> issueCoupon(
            @PathVariable(value = "couponId") Long couponId, @PathVariable(value = "userId") String userId

    ) {
        return ResponseEntity.ok(couponService.issueCoupon(couponId, userId));
    }

    @PostMapping("/create-coupon")
    public String createCoupon(@RequestBody CouponRequestDto couponRequestDto) {
        return couponService.createCoupon(couponRequestDto);
    }


}
