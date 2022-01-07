package com.w.zklock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

import java.util.concurrent.TimeUnit;

/**
 * 原理跟ETCD相差无几
 * 支持可重入的排它锁
 */
//@Component
@Slf4j
public class ZookeeperLock  {

    /**
     * Zookeeper客户端
     */
    private CuratorFramework client;

    private String address;

    private String lockPath;

    private InterProcessLock lock;

    ThreadLocal<InterProcessLock> lock_tl = new ThreadLocal<>();

    public ZookeeperLock(String address) {
        client = CuratorFrameworkFactory.newClient(address, new RetryNTimes(5, 5000));
        client.start();
        if (client.getState() == CuratorFrameworkState.STARTED) {
            log.info("zk client start successfully! ---- zkAddress:{}", address);
        } else {
            throw new RuntimeException("zk client start error...");
        }
    }

    public ZookeeperLock(String address, String lockPath) {
        client = CuratorFrameworkFactory.newClient(address, new RetryNTimes(5, 5000));
        client.start();
        if (client.getState() == CuratorFrameworkState.STARTED) {
            log.info("zk client start successfully! ---- zkAddress:{}", address);
        } else {
            throw new RuntimeException("zk client start error...");
        }

        this.lock = new InterProcessMutex(client, lockPath);
    }

    public void init(String lockPath) {
        lock_tl.set(new InterProcessMutex(client, lockPath));
    }

    public void lock() {
        try {
            lock_tl.get().acquire();
//            lock.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean tryLock() {
        boolean flag ;
        try {
            flag=this.lock_tl.get().acquire(0, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        boolean flag ;
        try {
            flag=this.lock_tl.get().acquire(time,unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    public void unlock() {
        try {
            lock_tl.get().release();
//            lock.release();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
