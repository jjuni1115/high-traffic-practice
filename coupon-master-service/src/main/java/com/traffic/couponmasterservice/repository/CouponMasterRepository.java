package com.traffic.couponmasterservice.repository;


import com.traffic.couponmasterservice.entity.CouponMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponMasterRepository extends JpaRepository<CouponMasterEntity, Long> {



}