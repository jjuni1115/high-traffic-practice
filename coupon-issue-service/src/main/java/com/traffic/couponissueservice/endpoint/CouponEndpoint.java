package com.traffic.couponissueservice.endpoint;

import com.traffic.couponissueservice.service.CouponService;
import dto.CouponRequestDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponEndpoint {

    private final CouponService couponService;


    @PostMapping("/issue/{couponId}/{userId}")
    public String issueCoupon(
            @PathVariable(value = "couponId") Long couponId, @PathVariable(value = "userId") String userId

    ) {
        return couponService.issueCoupon(couponId,userId);
    }

    @PostMapping("/create-coupon")
    public String createCoupon(@RequestBody CouponRequestDto couponRequestDto) {
        return couponService.createCoupon(couponRequestDto);
    }


}
