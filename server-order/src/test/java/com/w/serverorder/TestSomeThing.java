package com.w.serverorder;

import com.w.serverorder.lock.EtcdLock;
import com.w.serverorder.service.impl.EtcdLockTestService;
import org.junit.Test;
//import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestSomeThing {

    @Test
    public void test() throws InterruptedException {
        int threadCount = 2;
        EtcdLock etcdLock = new EtcdLock();
        EtcdLockTestService etcdLockTestService = new EtcdLockTestService(etcdLock);

        ExecutorService executorService = Executors.newFixedThreadPool(1000);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(()->{
                etcdLockTestService.incr();
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println("count:" + etcdLockTestService.getCount());
    }
}
