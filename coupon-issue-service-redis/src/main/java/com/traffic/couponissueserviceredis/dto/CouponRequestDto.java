package com.traffic.couponissueserviceredis.dto;

public record CouponRequestDto(String couponName, Long amount, String expireDate) {
}
