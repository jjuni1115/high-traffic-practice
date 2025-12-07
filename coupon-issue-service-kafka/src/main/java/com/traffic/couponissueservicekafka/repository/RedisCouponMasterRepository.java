package com.traffic.couponissueservicekafka.repository;

import com.traffic.couponissueservicekafka.entity.RedisCouponMaster;
import org.springframework.data.repository.CrudRepository;

public interface RedisCouponMasterRepository extends CrudRepository<RedisCouponMaster,Long> {
}
