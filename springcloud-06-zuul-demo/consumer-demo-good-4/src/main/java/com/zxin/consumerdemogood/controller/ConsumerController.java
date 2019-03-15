package com.zxin.consumerdemogood.controller;

import com.zxin.consumerdemogood.client.UserClient;
import com.zxin.consumerdemogood.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("consumer")
public class ConsumerController {

    @Autowired
    private UserClient userClient;

    @GetMapping("{id}")
    public User queryById(@PathVariable("id") Integer id){
        return userClient.queryById(id);
    }
}
