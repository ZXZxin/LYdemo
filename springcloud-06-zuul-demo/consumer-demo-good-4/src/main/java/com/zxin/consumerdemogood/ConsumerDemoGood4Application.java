package com.zxin.consumerdemogood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class ConsumerDemoGood4Application {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerDemoGood4Application.class, args);
	}

}
