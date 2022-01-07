package com.w.serverorder.lock;

import org.apache.commons.lang.StringUtils;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

//@Component
public class RedLock extends AbstractLock {

    @Resource
    @Qualifier("redissonRed1")
    RedissonClient redissonRed1;
    @Resource
    @Qualifier("redissonRed2")
    RedissonClient redissonRed2;
    @Resource
    @Qualifier("redissonRed3")
    RedissonClient redissonRed3;

    private ThreadLocal<String> key = new ThreadLocal<>();
    private ThreadLocal<RedissonRedLock> lockThreadLocal;

    @Override
    public void lock() {
        String keyStr = key.get();
        if(StringUtils.isEmpty(keyStr)){
            throw new RuntimeException("未提供key");
        }

        RLock lock1 = redissonRed1.getLock(keyStr.intern());
        RLock lock2 = redissonRed2.getLock(keyStr.intern());
        RLock lock3 = redissonRed3.getLock(keyStr.intern());
        RedissonRedLock lock = new RedissonRedLock(lock1, lock2, lock3);
        lockThreadLocal.set(lock);
        lock.lock();
    }

    @Override
    public void unlock() {
        RedissonRedLock redissonRedLock = lockThreadLocal.get();
        if(null == redissonRedLock){
            throw new RuntimeException("未加锁，不能解锁");
        }

        redissonRedLock.unlock();
    }

    public void setKey(String key) {
        this.key.set(key);
    }
}
