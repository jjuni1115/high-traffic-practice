package com.traffic.couponissueservice.dto;

public record CouponRequestDto(String couponName, Long amount, String expireDate) {
}
