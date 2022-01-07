package com.w.serverorder.service.impl;

import com.w.serverorder.entity.OrderLock;
import com.w.serverorder.lock.MyRedisDistributeLock;
import com.w.serverorder.service.OrderLockService;
import com.w.serverorder.service.OrderServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/*
 * 手写通过redis简单实现锁，工作中一般不用
 *
 * 几个常见的面试问题解决
    问题一：是否有加不上锁的时候
    回答：设值和过期时间应该在一条语句完成
           Boolean isSuccess = redis.opsForValue().setIfAbsent(lockKey.intern(), driverId + "");
           if (isSuccess != null && isSuccess)
               redis.expire(lockKey.intern(), 30L, TimeUnit.MINUTES);
    问题二：如果锁没执行到释放，比如业务逻辑运行到一般，运维重启电脑,或服务器挂了，没走finally，怎么办？
    回答： 每个锁都要加过期时间
 *
 * */
//@Service
public class MyRedisOrderLockService implements OrderLockService {

    @Resource
    @Qualifier("myRedisDistributeLock")
    MyRedisDistributeLock lock;

    @Resource
    OrderServer orderServer;

    @Override
    public boolean grabOrder(int orderId, Integer driverId) {

        String lockKey = "order_" + orderId;

        OrderLock entity = new OrderLock();
        entity.setOrderId(orderId);
        entity.setDriverId(driverId);

        lock.getLockKey().set(lockKey.intern());
        lock.getLockValue().set(driverId+"");

        lock.lock();

        try {
            // 执行业务代码
            boolean result = orderServer.grapOrder(orderId, driverId);
            if (result) {
                System.out.println("抢单成功：" + entity);
            } else {
                System.out.println("抢单失败：" + entity);
            }


        } finally {
            lock.unlock();
        }

        return false;
    }
}
