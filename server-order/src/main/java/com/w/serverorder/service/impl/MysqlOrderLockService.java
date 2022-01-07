package com.w.serverorder.service.impl;

import com.w.serverorder.dao.OrderLockDao;
import com.w.serverorder.entity.OrderLock;
import com.w.serverorder.lock.AbstractLock;
import com.w.serverorder.lock.MysqlDistributeLock;
import com.w.serverorder.service.OrderLockService;
import com.w.serverorder.service.OrderServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MysqlOrderLockService implements OrderLockService {
    @Resource
    OrderLockDao lockDao;

    @Resource
    OrderServer orderServer;

    @Resource
    @Qualifier("mysqlDistributeLock")
    AbstractLock lock;

    @Override
    public boolean grabOrder(int orderId, Integer driverId) {

        OrderLock entity = new OrderLock();
        entity.setOrderId(orderId);
        entity.setDriverId(driverId);

        // 注册回调：tryLock
        ((MysqlDistributeLock) lock).getLockSupplier().set(() -> {
            // 锁表-表需要主键冲突
            // 当前表 同一个订单 只能存在一条数据
            System.out.println("获取锁中:" + entity);
            try {
                return lockDao.insert(entity);

            } catch (Exception e) {
                System.out.println("获取锁失败:" + entity);
                return false;
            }
        });
        // 注册回调：unlock
        ((MysqlDistributeLock) lock).getUnLockSupplier().set(() -> {
            // 异常的时候会调用
            System.out.println("解锁订单:" + orderId);
            return lockDao.delete(orderId) > 0;
        });

        // 加锁
        lock.lock();

        System.out.println("司机[" + driverId + "]获得了订单[" + orderId + "]");

        try {
            // 执行业务
            // 处理订单表-设置订单真正的所属司机
            boolean result = orderServer.grapOrder(orderId, driverId);
            if (result) {
                System.out.println("抢单成功：" + entity);
            } else {
                System.out.println("抢单失败：" + entity);
            }
        } finally {
            // 解锁
            lock.unlock();
        }

        return true;
    }
}
