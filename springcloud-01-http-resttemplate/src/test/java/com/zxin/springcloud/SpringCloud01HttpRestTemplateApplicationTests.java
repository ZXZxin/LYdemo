package com.zxin.springcloud;

import com.zxin.springcloud.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringCloud01HttpRestTemplateApplicationTests {

	@Autowired
	private RestTemplate restTemplate;

	@Test
	public void contextLoads() {
		// 本来拿到的是Json，但是帮你转换了
		User user = restTemplate.getForObject("http://localhost:8080/user/1", User.class);

		System.out.println("user = " + user);
	}
}
