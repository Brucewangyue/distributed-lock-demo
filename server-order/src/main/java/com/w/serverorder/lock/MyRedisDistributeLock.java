package com.w.serverorder.lock;

import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 适合中小型应用
 * 还需优化的问题
 *  当锁被占用时，其他人在疯狂请求redis
 *      改为，使用lua脚本，如果加锁失败，返回锁的过期时间，根据锁的返回时长做优化 ？？
 *  改为 hash 类型，value使用（当前线程Id + 连接标识）
 *  锁重入
 *      如果是当前线程继续抢锁，那么就把value++；
 *      解锁的时候一直减到0
 *
 */
//@Component
@Data
public class MyRedisDistributeLock extends AbstractLock {

    ThreadLocal<String> lockKey = new ThreadLocal<>();
    ThreadLocal<String> lockValue = new ThreadLocal<>();
    ThreadLocal<Long> expireSeconds = new ThreadLocal<>();

    @Resource
    StringRedisTemplate redis;

    @Resource
    MyRedisLockRefresh lockRefresh;

    @Override
    public void lock() {
        if (tryLock()) {
            System.out.println(lockKey.get() + "_" + lockValue.get() + "：加锁成功!");
            // 锁刷新
            lockRefresh.refresh(lockKey.get().intern(), lockValue.get().intern(), expireSeconds.get());
            return;
        }

        System.out.println(lockKey.get() + "_" + lockValue.get() + "：加锁失败!");

        // todo 这里需要优化，减少等待抢锁的线程疯狂递归调用
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 递归等待锁
        lock();
    }

    public boolean tryLock() {
        try {
            // setIfAbsent：不会重复或者覆盖key名
            Long expire = expireSeconds.get();
            if (expire == null)
                expire = 30L;

            // 设置key 这里最好用intern 否则可能不是锁的同一个字符串
            Boolean isSuccess = redis.opsForValue().setIfAbsent(lockKey.get().intern(), lockValue.get().intern(), expire, TimeUnit.SECONDS);

            return isSuccess != null && isSuccess;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void unlock() {
        // 这里防止业务逻辑的执行时间超过了锁的过期时间，导致删除的是别人的锁（做了锁续约，除非续约失败（续约的请求刚好网络有问题），否则这里不会删除别人的锁）
        // todo 线上环境这里的两个操作要放在一个原子里面操作，用lua脚本来实现
        String value = redis.opsForValue().get(lockKey.get().intern());
        if (value != null && value.equals(lockValue.get())) {
            redis.delete(lockKey.get().intern());
        }

        lockKey.remove();
        lockValue.remove();
        expireSeconds.remove();
    }
}
