package com.traffic.couponissueservice.dto;

public record CouponResponseDto(Long id, String couponName, Long amount, String expireDate) {
}
