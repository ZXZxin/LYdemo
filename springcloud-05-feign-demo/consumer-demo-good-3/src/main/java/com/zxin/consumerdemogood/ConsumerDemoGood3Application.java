package com.zxin.consumerdemogood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringCloudApplication
@EnableFeignClients
public class ConsumerDemoGood3Application {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerDemoGood3Application.class, args);
	}

}
