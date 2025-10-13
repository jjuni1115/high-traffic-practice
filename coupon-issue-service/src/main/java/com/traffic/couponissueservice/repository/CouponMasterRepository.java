package com.traffic.couponissueservice.repository;

import com.traffic.couponissueservice.entity.CouponMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponMasterRepository extends JpaRepository<CouponMasterEntity, Long> {



}