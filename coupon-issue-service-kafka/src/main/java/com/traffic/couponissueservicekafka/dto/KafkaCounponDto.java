package com.traffic.couponissueservicekafka.dto;

import lombok.Builder;

@Builder
public record KafkaCounponDto(

        Long couponId
        , String userId

) {
}
