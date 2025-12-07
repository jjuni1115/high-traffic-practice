package com.traffic.couponmasterservice.dto;

import lombok.Builder;

@Builder
public record KafkaCounponDto(

        Long couponId
        , String userId

) {
}
