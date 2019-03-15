package com.zxin.consumerdemogood.controller;

import com.zxin.consumerdemogood.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
@RequestMapping("consumer")
public class ConsumerController {

    @Autowired
    private RestTemplate restTemplate;

    // 没有使用 负载均衡的写法
//    @Autowired
//    private DiscoveryClient discoveryClient;
//
//    @GetMapping("{id}")
//    @ResponseBody // 如果上面不是RestController 就一定要记得写这个
//    public User queryById(@PathVariable("id") Integer id){
//
//        // 每一个服务下面可以包含很多实例(多台机器实现负载均衡)，因为我们只有一台机器，所以就直接获取instances.get(0)即可
//        List<ServiceInstance> instances = discoveryClient.getInstances("user-service-good");
//        ServiceInstance instance = instances.get(0);
//
//
//        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/user/" + id;
//        User user = restTemplate.getForObject(url, User.class);
//        return user;
//    }

    @GetMapping("{id}")
    @ResponseBody // 如果上面不是RestController 就一定要记得写这个
    public User queryById(@PathVariable("id") Integer id) {
        // 帮我们实现了负载均衡
        String url = "http://user-service-good/user/" + id;
        User user = restTemplate.getForObject(url, User.class);
        return user;
    }
}
