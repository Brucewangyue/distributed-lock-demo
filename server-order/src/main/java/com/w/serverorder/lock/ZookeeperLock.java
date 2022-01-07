package com.w.serverorder.lock;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 原理跟ETCD相差无几
 * 支持可重入的排它锁
 */
//@Component
@Slf4j
public class ZookeeperLock extends AbstractLock {

    /**
     * Zookeeper客户端
     */
    private CuratorFramework client;

    ThreadLocal<InterProcessLock> lock_tl = new ThreadLocal<>();

    ConcurrentMap<String, InterProcessMutex> lockInterProcessMutex = Maps.newConcurrentMap();

    public ZookeeperLock(String address) {
        client = CuratorFrameworkFactory.newClient(address, new RetryNTimes(5, 5000));
        client.start();
        if (client.getState() == CuratorFrameworkState.STARTED) {
            log.info("zk client start successfully! ---- zkAddress:{}", address);
        } else {
            throw new RuntimeException("zk client start error...");
        }
    }

    public void init(String lockPath) {
        lock_tl.set(new InterProcessMutex(client, lockPath));
//        InterProcessMutex interProcessMutex = lockInterProcessMutex.get(lockPath);
//        if (null != interProcessMutex) {
//            lock_tl.set(interProcessMutex);
//        } else {
//            // todo 这里需要加单机锁
//            InterProcessMutex newInterProcessMutex = new InterProcessMutex(client, lockPath);
//            lock_tl.set(newInterProcessMutex);
//            lockInterProcessMutex.put(lockPath, newInterProcessMutex);
//        }
    }

    @Override
    public void lock() {
        try {
            lock_tl.get().acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean tryLock() {
        boolean flag;
        try {
            flag = this.lock_tl.get().acquire(0, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        boolean flag;
        try {
            flag = this.lock_tl.get().acquire(time, unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    @Override
    public void unlock() {
        try {
            lock_tl.get().release();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
