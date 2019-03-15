package com.zxin.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServer4Application {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServer4Application.class, args);
	}

}
