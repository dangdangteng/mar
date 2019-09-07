package com.dinglicom.mr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.dinglicom.mr.repository", "com.dinglicom.mr.controller.view", "com.dinglicom.mr.controller.job", "com.dinglicom.mr.producer", "com.dinglicom.mr.service", "com.dinglicom.mr.producer.confirm"})
@EnableJpaAuditing
@EnableDiscoveryClient
public class MarApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarApplication.class, args);
	}

	@Autowired
	private ApplicationContext appContext;

	public void initiateShutdown(int returnCode) {
		SpringApplication.exit(appContext, () -> returnCode);
	}

}
