package com.traffic.couponissueserviceredis.repository;


import com.traffic.couponissueserviceredis.entity.CouponMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponMasterRepository extends JpaRepository<CouponMasterEntity, Long> {



}