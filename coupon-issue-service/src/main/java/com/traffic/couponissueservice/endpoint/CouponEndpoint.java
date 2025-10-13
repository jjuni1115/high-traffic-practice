package com.traffic.couponissueservice.endpoint;

import com.traffic.couponissueservice.service.CouponService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponEndpoint {

    private final CouponService couponService;


    @PostMapping("/issue")
    public String issueCoupon() {
        return couponService.issueCoupon(1L,"");
    }


}
