package com.traffic.couponissueserviceredis.repository;

import com.traffic.couponissueserviceredis.entity.RedisCouponMaster;
import org.springframework.data.repository.CrudRepository;

public interface RedisCouponMasterRepository extends CrudRepository<RedisCouponMaster,Long> {
}
