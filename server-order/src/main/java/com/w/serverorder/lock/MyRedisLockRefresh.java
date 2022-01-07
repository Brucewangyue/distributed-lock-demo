package com.w.serverorder.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 刷新锁的过期时间，让业务在执行期间，锁永远不过期
 *  开启一个线程，判断当前的锁是否还在，如果在就递归延期，不在就结束线程
 */
@Component
public class MyRedisLockRefresh {

    @Resource
    StringRedisTemplate redis;

    @Async
    void refresh(String key, String value, long expire) {

        // 这里是每到过期时间的1/3就开始刷新
        long sleepTime = expire / 3;

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String lockValue = redis.opsForValue().get(key);
        if (lockValue == null) {
            return;
        }

        if (lockValue.equals(value)) {
            // 这里的 TimeUnit.SECONDS 写死，只是用于测试
            redis.expire(key, expire, TimeUnit.SECONDS);
            refresh(key, value, expire);
        }
    }
}
