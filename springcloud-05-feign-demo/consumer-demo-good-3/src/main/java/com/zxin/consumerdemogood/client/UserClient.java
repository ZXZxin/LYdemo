package com.zxin.consumerdemogood.client;

import com.zxin.consumerdemogood.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Feign的配置  底层使用的是动态代理
@FeignClient(value = "user-service-good-3", fallback = UserFeignClientFallback.class)  // 服务的名称
public interface UserClient {

    @GetMapping("user/{id}")
    User queryById(@PathVariable("id") Integer id);
}
