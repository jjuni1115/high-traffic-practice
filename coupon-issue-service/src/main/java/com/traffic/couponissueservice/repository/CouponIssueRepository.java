package com.traffic.couponissueservice.repository;

import com.traffic.couponissueservice.entity.CouponIssueEntity;
import com.traffic.couponissueservice.entity.CouponMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueRepository extends JpaRepository<CouponIssueEntity, Long> {

    CouponIssueEntity findByCouponMasterAndUserId(CouponMasterEntity couponMaster, String userId);

    Integer countByCouponMasterAndUserId(CouponMasterEntity couponMaster, String userId);
}