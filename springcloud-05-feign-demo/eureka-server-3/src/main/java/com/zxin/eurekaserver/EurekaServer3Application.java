package com.zxin.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer// 声明这个应用是一个EurekaServer
public class EurekaServer3Application {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServer3Application.class, args);
	}

}
