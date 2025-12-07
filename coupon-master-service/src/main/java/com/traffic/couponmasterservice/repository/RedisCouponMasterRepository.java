package com.traffic.couponmasterservice.repository;

import com.traffic.couponmasterservice.entity.RedisCouponMaster;
import org.springframework.data.repository.CrudRepository;

public interface RedisCouponMasterRepository extends CrudRepository<RedisCouponMaster,Long> {
}
