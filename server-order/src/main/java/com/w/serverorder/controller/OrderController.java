package com.w.serverorder.controller;

import com.w.serverorder.service.OrderLockService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController()
@RequestMapping("order")
public class OrderController {

    @Resource
//    @Qualifier("mysqlOrderLockService")
//    @Qualifier("myRedisOrderLockService")
//    @Qualifier("singleRedissonOrderLockService")
//    @Qualifier("redissonRedLockOrderLockService")
    @Qualifier("annotationLockOrderServer")
    OrderLockService orderService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("testRedis")
    public String testRedis() {
        String value = stringRedisTemplate.opsForValue().get("k1");
        return value;
    }

    @PostMapping("grabOrder")//
    public String grabOrder(@RequestParam("orderId") int orderId, @RequestParam("driverId") int driverId) {
        orderService.grabOrder(orderId, driverId);
        return "success";
    }
}
