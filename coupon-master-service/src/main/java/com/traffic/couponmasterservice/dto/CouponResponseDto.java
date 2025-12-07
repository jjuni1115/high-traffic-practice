package com.traffic.couponmasterservice.dto;

public record CouponResponseDto(Long id, String couponName, Long amount, String expireDate) {
}
