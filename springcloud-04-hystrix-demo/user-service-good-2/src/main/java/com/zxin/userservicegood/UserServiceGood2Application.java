package com.zxin.userservicegood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient // 这里不用 EnableEurekaClient，这个还可以用Zookper之类的(抽象了)
@MapperScan("com.zxin.userservicegood.mapper")
public class UserServiceGood2Application {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceGood2Application.class, args);
	}

}
