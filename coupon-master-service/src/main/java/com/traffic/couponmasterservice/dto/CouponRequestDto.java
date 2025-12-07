package com.traffic.couponmasterservice.dto;

public record CouponRequestDto(String couponName, Long amount, String expireDate) {
}
