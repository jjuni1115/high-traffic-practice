package com.traffic.couponissueserviceredis.dto;

public record CouponResponseDto(Long id, String couponName, Long amount, String expireDate) {
}
