package com.traffic.couponissueservicekafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CouponIssueServiceKafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponIssueServiceKafkaApplication.class, args);
    }

}
