package com.w.serverorder.service.impl;

import com.w.serverorder.service.OrderLockService;
import com.w.serverorder.service.OrderServer;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 适合中小型项目
 * 问题：
 * redis单机问题，中小型项目完全可以只使用单台redis服务器做锁管理
 * （据说高德的打车交易模块的redis就是用单机，因为高德用了阿里的云服务，阿里本身就提供了99.9999%的保障）
 */
//@Service
public class SingleRedissonOrderLockService implements OrderLockService {

    @Resource
    OrderServer orderServer;

    @Resource
    @Qualifier("redissonClient")
    RedissonClient redissonClient;

    @Override
    public boolean grabOrder(int orderId, Integer driverId) {

        String lockKey = "order_" + orderId;
        RLock lock = redissonClient.getLock(lockKey.intern());
        System.out.println("开始抢单：orderId=" + orderId + ",driverId=" + driverId);

        try {
            // 此代码默认 设置key 超时时间30秒，过10秒（1/3），再延时
            lock.lock();
            // 执行业务代码
            boolean result = orderServer.grapOrder(orderId, driverId);
            if (result) {
                System.out.println("抢单成功：orderId=" + orderId + ",driverId=" + driverId);
                return true;
            } else {
                System.out.println("抢单失败：orderId=" + orderId + ",driverId=" + driverId);
            }

        } finally {
            lock.unlock();
        }

        return false;
    }
}
