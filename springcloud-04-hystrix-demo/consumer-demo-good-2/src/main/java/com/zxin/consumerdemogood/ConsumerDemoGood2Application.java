package com.zxin.consumerdemogood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

//@SpringBootApplication
//@EnableDiscoveryClient
////@EnableHystrix
//@EnableCircuitBreaker // 这个比上面那个更EnableHystrix好，因为我们后面会用熔断
// 上面三个注解可以用SpringCloudApplication
@SpringCloudApplication
public class ConsumerDemoGood2Application {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerDemoGood2Application.class, args);
	}

	@Bean
	@LoadBalanced // 实现Ribbon负载均衡
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

}
