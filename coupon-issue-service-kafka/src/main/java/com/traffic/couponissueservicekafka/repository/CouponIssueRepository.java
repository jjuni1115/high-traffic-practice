package com.traffic.couponissueservicekafka.repository;


import com.traffic.couponissueservicekafka.entity.CouponIssueEntity;
import com.traffic.couponissueservicekafka.entity.CouponMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponIssueRepository extends JpaRepository<CouponIssueEntity, Long> {

    CouponIssueEntity findByCouponMaster_IdAndUserId(Long couponId, String userId);

    Integer countByCouponMasterAndUserId(CouponMasterEntity couponMaster, String userId);
}