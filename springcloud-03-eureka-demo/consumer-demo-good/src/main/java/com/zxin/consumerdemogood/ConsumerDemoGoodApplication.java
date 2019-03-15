package com.zxin.consumerdemogood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class ConsumerDemoGoodApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerDemoGoodApplication.class, args);
	}

	@Bean
	@LoadBalanced // 实现Ribbon负载均衡
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

}
