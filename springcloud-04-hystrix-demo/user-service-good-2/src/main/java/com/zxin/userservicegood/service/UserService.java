package com.zxin.userservicegood.service;

import com.zxin.userservicegood.mapper.UserMapper;
import com.zxin.userservicegood.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User queryById(Integer id){
        try {
            Thread.sleep(2000L);  //这里是模拟服务器的延时，然后Hystrix就会进行降级处理
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userMapper.selectByPrimaryKey(id);
    }

    @Transactional  // 事务
    public void insertUser(User user){
        userMapper.insert(user);
    }
}

