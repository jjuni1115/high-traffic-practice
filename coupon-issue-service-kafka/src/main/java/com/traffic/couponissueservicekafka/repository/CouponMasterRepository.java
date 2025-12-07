package com.traffic.couponissueservicekafka.repository;


import com.traffic.couponissueservicekafka.entity.CouponMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponMasterRepository extends JpaRepository<CouponMasterEntity, Long> {



}