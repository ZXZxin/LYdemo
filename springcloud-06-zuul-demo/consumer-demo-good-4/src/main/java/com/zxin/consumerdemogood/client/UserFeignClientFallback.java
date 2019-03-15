package com.zxin.consumerdemogood.client;

import com.zxin.consumerdemogood.pojo.User;
import org.springframework.stereotype.Component;

@Component
public class UserFeignClientFallback implements UserClient  {
    // 写熔断的配置逻辑
    @Override
    public User queryById(Integer id) {
        User user = new User();
        user.setLastName("未知用户!");
        return user;
    }
}
