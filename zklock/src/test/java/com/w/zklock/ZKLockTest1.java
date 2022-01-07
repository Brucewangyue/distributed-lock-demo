package com.w.zklock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ZKLockTest1 {
    private ExecutorService executorService = Executors.newCachedThreadPool();

    ZookeeperLock lock = new ZookeeperLock("10.0.0.99:2181","/lockPath");

    @Test
    public void testLock() throws InterruptedException {
        String lockPath =  "/testLock";
        int[] num = {0};
        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            executorService.submit(()->{
                try{
                    lock.init(lockPath);
                    lock.lock();
                    log.info("获得所，开始执行业务");
                    num[0]++;
                }catch (Exception e){
                    throw new RuntimeException(e);
                }finally {
                    lock.unlock();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
        log.info("耗时:{}",System.currentTimeMillis()-start);
        System.out.println(num[0]);
    }

    @Test
    public void testNoLock() throws Exception{
        long start = System.currentTimeMillis();
        int[] num = {0};
        for(int i=0;i<1000;i++){
            executorService.submit(()->{
                num[0]++;
            });

        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
        log.info("耗时:{}",System.currentTimeMillis()-start);
        System.out.println(num[0]);
    }

    @Test
    public void testTrySuccess() throws InterruptedException {
        String lockPath =  "/lock/testLockPath";
//        lock.init(lockPath);

        boolean b = lock.tryLock();
        try {
            if(b){
                log.info("{}获取锁成功",Thread.currentThread().getName() );
                TimeUnit.MINUTES.sleep(2);
            }else{
                log.info("{}获取锁失败",Thread.currentThread().getName() );
            }
        }finally {
            lock.unlock();
        }
    }

    @Test
    public void testTryTimeout(){
        String lockPath =  "/lock/testLockPath";
//        lock.init(lockPath);

        Thread thread1 = new Thread(() -> {
            lock.lock();
            try {
                log.info("{}获得锁",Thread.currentThread().getName());
                // 占有锁10秒
                Thread.sleep(10000);
            } catch (Exception e) {
            } finally {
                lock.unlock();
                log.info("{}释放锁",Thread.currentThread().getName());
            }
        }, "thread1");
        thread1.start();

        // 睡两秒
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        // 尝试获取锁，超时时间3秒
        Thread thread2 = new Thread(() -> {
            log.info("{}尝试获得锁",Thread.currentThread().getName());
            boolean b = false;
            try {
                b = lock.tryLock(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(b){
                try {
                    log.info("{}获取锁成功",Thread.currentThread().getName() );
                } catch (Exception e) {
                } finally {
                    lock.unlock();
                }
            }else{
                log.info("{}获取锁失败",Thread.currentThread().getName() );
            }
        }, "thread2");
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
