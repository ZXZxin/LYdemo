package com.zxin.springboot.service;

import com.zxin.springboot.mapper.UserMapper;
import com.zxin.springboot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User queryById(Integer id){
        return userMapper.selectByPrimaryKey(id);
    }

    @Transactional  // 事务
    public void insertUser(User user){
        userMapper.insert(user);
    }
}

