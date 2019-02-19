package com.zxin.springboot;

//import org.mybatis.spring.annotation.MapperScan;
import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zxin.springboot.mapper")
public class SpringBoot02SimpleCrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBoot02SimpleCrudApplication.class, args);
	}

}
