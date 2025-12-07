package com.traffic.couponissueservicekafka.dto;

public record CouponResponseDto(Long id, String couponName, Long amount, String expireDate) {
}
