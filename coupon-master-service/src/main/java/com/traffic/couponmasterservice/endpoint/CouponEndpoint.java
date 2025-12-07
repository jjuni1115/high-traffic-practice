package com.traffic.couponmasterservice.endpoint;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.traffic.couponmasterservice.dto.CouponRequestDto;
import com.traffic.couponmasterservice.dto.CouponResponseDto;
import com.traffic.couponmasterservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponEndpoint {

    private final CouponService couponService;


    @PostMapping("/create-coupon")
    public String createCoupon(@RequestBody CouponRequestDto couponRequestDto) {
        return couponService.createCoupon(couponRequestDto);
    }

    @GetMapping("/{couponId}/{userId}")
    public ResponseEntity<String> getUserCoupon(
            @PathVariable(value = "couponId") Long couponId,
            @PathVariable(value = "userId") String userId){

        return ResponseEntity.ok(couponService.getUserCoupon(couponId, userId));

    }

    @GetMapping("/view/{couponId}")
    public ResponseEntity<CouponResponseDto> viewCoupon(@PathVariable(value = "couponId") Long couponId){
        return ResponseEntity.ok(couponService.viewCoupon(couponId));
    }


}
