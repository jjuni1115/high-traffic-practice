package com.traffic.couponissueserviceredis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_master")
@Getter
@Setter
public class CouponMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String couponName;
    private Long amount;
    private LocalDateTime expireDate;


}
