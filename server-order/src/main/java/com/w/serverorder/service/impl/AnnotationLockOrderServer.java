package com.w.serverorder.service.impl;

import com.w.serverorder.annotation.DistributedLock;
import com.w.serverorder.service.OrderLockService;
import com.w.serverorder.service.OrderServer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 通过注解+AOP无侵入实现分布式锁
 */
@Service
public class AnnotationLockOrderServer implements OrderLockService {

    @Resource
    OrderServer orderServer;

    @DistributedLock(keyPrefix = "order_", fieldName = "orderId")
    @Override
    public boolean grabOrder(int orderId, Integer driverId) {

        // 执行业务代码
        boolean result = orderServer.grapOrder(orderId, driverId);
        if (result) {
            System.out.println("抢单成功：orderId=" + orderId + ",driverId=" + driverId);
            return true;
        } else {
            System.out.println("抢单失败：orderId=" + orderId + ",driverId=" + driverId);
        }

        return false;
    }
}
