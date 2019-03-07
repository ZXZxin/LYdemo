package com.zxin.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpringCloud01HttpRestTemplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringCloud01HttpRestTemplateApplication.class, args);
	}

	// 使用RestTemplate
	@Bean
	public RestTemplate restTemplate() {
		// 默认的RestTemplate，底层是走JDK的URLConnection方式。
		return new RestTemplate();
	}
}
