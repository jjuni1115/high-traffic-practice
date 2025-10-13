package com.traffic.couponissueservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "coupon_issue")
@Getter
@Setter
public class CouponIssueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_master_id", nullable = false)
    private CouponMasterEntity couponMaster;
    private String userId;


}
