package com.zxin.consumerdemogood.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.zxin.consumerdemogood.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("consumer")
@DefaultProperties(defaultFallback = "queryByIdFallback") // 这里是为了所有的方法都可以用这个降级逻辑处理
public class ConsumerController {

    @Autowired
    private RestTemplate restTemplate;

//    @GetMapping("{id}")
//    public User queryById(@PathVariable("id") Integer id) {
//        // 帮我们实现了负载均衡
//        String url = "http://user-service-good-2/user/" + id;
//        User user = restTemplate.getForObject(url, User.class);
//        return user;
//    }

    @GetMapping("{id}")
//    @HystrixCommand(fallbackMethod = "queryByIdFallback") // 开启失败容错处理, 这里不写了，上面类上加了
//    @HystrixCommand() // 开启失败容错处理, 这里不写了，上面类上加了
    // 下面的是局部的超时时长的配置
//    @HystrixCommand(commandProperties = {
//            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000") // 这里配置之后就不会有问题了，只睡眠了2000s
//    })
    // 使用熔断的配置
    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMillisecond", value = "10000"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),

    }
    )
    public String queryById(@PathVariable("id") Integer id) {
        if(id % 2 == 0){ //这里为了展示熔断机制 , 需要注释掉 user-service 中的Thread.sleep
            throw new RuntimeException("");
        }
        // 帮我们实现了负载均衡
        String url = "http://user-service-good-2/user/" + id;
//        User user = restTemplate.getForObject(url, User.class);
        String user = restTemplate.getForObject(url, String.class);
        return user;
    }

    // 必须保证和它对应的方法　的返回结果和参数一样　
//    public String queryByIdFallback(Integer id) {
    public String queryByIdFallback() {  //写在整体上，通用方法，就不能写参数了
            return "不好意思，服务器正忙...";
    }
}
