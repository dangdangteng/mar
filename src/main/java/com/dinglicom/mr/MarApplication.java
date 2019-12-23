package com.dinglicom.mr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.dinglicom.mr.repository", "com.dinglicom.mr.controller.view","com.dinglicom.mr.handle","com.dinglicom.mr.handle.service", "com.dinglicom.mr.controller.job", "com.dinglicom.mr.producer", "com.dinglicom.mr.service", "com.dinglicom.mr.producer.confirm","com.dinglicom.mr.feign.fallback","com.dinglicom.mr.feign","com.dinglicom.mr.config","com.dinglicom.mr.task"})
@EnableJpaAuditing
@EnableEurekaClient
@EnableHystrix
//允许线程熔断
@EnableCircuitBreaker
//启用事务处理
@EnableTransactionManagement(proxyTargetClass = true)
@EnableFeignClients(basePackages = {"com.dinglicom.mr.feign","com.dinglicom.mr.feign.fallback"})
public class MarApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarApplication.class, args);
	}
}
