package com.w.serverorder.service.impl;

import com.w.serverorder.lock.RedLock;
import com.w.serverorder.service.OrderLockService;
import com.w.serverorder.service.OrderServer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/*
 * 适合大型项目
 * redis红锁
 * 这里的3台redis没有任何关系
 *   解决了redis单机的问题
 * */
//@Service
public class RedissonRedLockOrderLockService implements OrderLockService {

    @Resource
    OrderServer orderServer;

    @Resource
    RedLock lock;


    @Override
    public boolean grabOrder(int orderId, Integer driverId) {
        String lockKey = "order_" + orderId;

        lock.setKey(lockKey);

        System.out.println("开始抢单：orderId=" + orderId + ",driverId=" + driverId);

        try {
            // 此代码默认 设置key 超时时间30秒，过10秒（1/3），再延时
            lock.lock();

            // 测试key续期
            try {
                Thread.sleep(3 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
