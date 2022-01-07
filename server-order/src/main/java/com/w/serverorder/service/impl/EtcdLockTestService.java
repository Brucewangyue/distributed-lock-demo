package com.w.serverorder.service.impl;

import com.w.serverorder.lock.EtcdLock;
import io.etcd.jetcd.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 适合中大型项目
 */
@Service
@Slf4j
public class EtcdLockTestService {

    @Resource
    EtcdLock lock;

    public EtcdLockTestService(EtcdLock lock) {
        this.lock = lock;
    }

    int[] count = {0};

    public void incr() {

        String lockKey = "test_count";
        lock.init(lockKey, 10);

        try {
            lock.lock();
            log.info("获取锁成功，开始执行业务");
            Thread.sleep(30 * 1000);
            count[0]++;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public int getCount() {
        return count[0];
    }


    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        int[] count = {0};
        int threadCount = 3;

        ExecutorService executorService = Executors.newFixedThreadPool(1000);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(()->{
                EtcdLock lock = new EtcdLock();
                String lockKey = "test_count";
                lock.init(lockKey, 10);
                try {
                    lock.lock();
                    System.out.println("执行业务逻辑");
                    TimeUnit.SECONDS.sleep(10);
                    count[0]++;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        lock.unlock();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        System.err.println("执行结果: " + count[0]);
    }

}
