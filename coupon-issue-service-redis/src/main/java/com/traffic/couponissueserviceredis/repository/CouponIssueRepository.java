package com.traffic.couponissueserviceredis.repository;


import com.traffic.couponissueserviceredis.entity.CouponIssueEntity;
import com.traffic.couponissueserviceredis.entity.CouponMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponIssueRepository extends JpaRepository<CouponIssueEntity, Long> {

    CouponIssueEntity findByCouponMasterAndUserId(CouponMasterEntity couponMaster, String userId);

    Integer countByCouponMasterAndUserId(CouponMasterEntity couponMaster, String userId);
}