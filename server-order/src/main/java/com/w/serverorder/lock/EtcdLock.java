package com.w.serverorder.lock;

import com.google.common.collect.Maps;
import io.etcd.jetcd.*;
import io.etcd.jetcd.lock.LockResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class EtcdLock extends AbstractLock {

    Client client;
    Lease leaseClient;
    Lock lockClient;
    //    Map<Thread, EtcdLockData> threadData = new HashMap<>();
    // 这里必须要用并发Map，不然高并发下put的时候，不管Key是否一样，都可能会覆盖
    ConcurrentMap<Thread, EtcdLock.LockData> threadData = Maps.newConcurrentMap();

    public EtcdLock() {
        client =
                Client.builder().endpoints("http://10.0.0.30:2379", "http://10.0.0.31:2379", "http://10.0.0.32:2379").build();
        leaseClient = client.getLeaseClient();
        lockClient = client.getLockClient();
    }

    public void init(String key) {
        init(key, 3L, TimeUnit.SECONDS);
    }

    public void init(String key, long lease) {
        init(key, lease, TimeUnit.SECONDS);
    }

    public void init(String key, long lease, TimeUnit timeUnit) {
        Thread currentThread = Thread.currentThread();
//        String etcdKey = "/lock/" + key + "/" + UUID.randomUUID();
        String etcdKey = "/lock/" + key;
        LockData lockData = new LockData();
        lockData.setLockKey(etcdKey);
        lockData.setCurrentThread(currentThread);

        // 统一转成纳秒，后面的计算只需要TimeUnit.NANOSECONDS.
        lockData.setLeaseTTL(timeUnit.toNanos(lease));
//        lockData.setTimeUnit(timeUnit);

        threadData.put(currentThread, lockData);
    }

    @Override
    public void lock() {
        Thread currentThread = Thread.currentThread();
        // 锁重入判断
        LockData lockData = threadData.get(currentThread);
        assert lockData != null;
        if (lockData.getLockCount().get() != 0) {
            log.info("锁重入");
            // 增加锁重入次数
            lockData.getLockCount().incrementAndGet();
            return;
        }

        try {
            // 创建租约 todo
            long leaseTLL = lockData.getLeaseTTL();
            // lease ttl unit seconds
            long leaseId = leaseClient.grant(TimeUnit.NANOSECONDS.toSeconds(leaseTLL)).get().getID();
            lockData.setLeaseId(leaseId);

            // 续约间隔为租约的 4/5
            long period = leaseTLL - leaseTLL / 5;

            // 定时续约，防止，防止等待锁时间过长中断
            lockData.getScheduledExecutorService().scheduleAtFixedRate(() -> {
                try {
                    leaseClient.keepAliveOnce(leaseId);
                    log.info("续约,leaseId:{}，锁路径:{}，线程:{}", leaseId, lockData.getLockPath(), lockData.getCurrentThread());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, period, period, TimeUnit.NANOSECONDS);

            // 加锁
            ByteSequence key = ByteSequence.from(lockData.getLockKey().getBytes());
            LockResponse lockResponse = lockClient.lock(key, leaseId).get();
            if (null != lockResponse) {
                // 加锁成功
                ByteSequence lockPath = lockResponse.getKey();
                lockData.setLockPath(lockPath);
                lockData.getLockCount().incrementAndGet();
                log.info("获取锁成功，锁路径:{},线程:{}", lockPath.toString(StandardCharsets.UTF_8), currentThread.getName());
            }
        } catch (Exception e) {
            log.error("获取锁失败", e);
        }
    }

    @Override
    public void unlock() {
        LockData lockData = getData();

        // 重入锁处理
        int newLockCount = lockData.getLockCount().decrementAndGet();
        if (newLockCount > 0) {
            return;
        }

        if (newLockCount < 0) {
            throw new IllegalMonitorStateException("锁的计数器小于等于0:" + lockData.getLockKey());
        }

        ByteSequence lockPath = lockData.getLockPath();
        if (lockPath == null) return;

        try {
            lockClient.unlock(lockPath).get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭定时任务
            lockData.getScheduledExecutorService().shutdown();
            // 删除租约
            leaseClient.revoke(lockData.getLeaseId());
            // 移除线程资源
            threadData.remove(lockData.getCurrentThread());
            log.info("解锁成功,leaseId:{},锁路径:{},线程:{}", lockData.getLeaseId(), lockData.getLockPath().toString(StandardCharsets.UTF_8), lockData.getCurrentThread());
        }
    }

    private LockData getData() {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadData.get(currentThread);
        if (null == lockData) {
            throw new RuntimeException("EtcdLock 你不拥有锁:");
        }

        return lockData;
    }

    private void etcd_lock_theory() {
        // 设置一个key，得到版本号
        // 通过key前缀，返回key列表，查看自己的版本号是否是最小的
        // 如果是则加锁成功，不是则watch前一个Key（revision比自己小，且最接近的一个）

        // 通过租约设置key
//            PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
//            PutResponse putResponse = kvClient.put(key, value, putOption).get();
//            Response.Header header = putResponse.getHeader();
//            long revision = header.getRevision();

        // 通过前缀去拉取列表
//            GetResponse getResponse = kvClient.get(key).get();
//            List<KeyValue> kvs = getResponse.getKvs();
//            kvs.stream().min()

        // 判断版本
//            if (true) {
        // 加锁成功
        // 创建租约
//                kvClient.
//                PutOption putOption = new PutOption();
//                PutResponse putResponse1 = kvClient.put(key, value,putOption).get();
//            } else {
        // 加锁失败
        // watch
//            }
    }

    private void etcd_unlock_theory() {
        //
//        KV kvClient = client.getKVClient();
//        ByteSequence key = ByteSequence.from(key_tl.get().getBytes());
    }

    @Data
    private static class LockData {
        /**
         * 租约Id
         */
        long leaseId;
        /**
         * 租约时长
         */
        long leaseTTL = 30;
        /**
         * 租约时长单位
         */
        TimeUnit timeUnit = TimeUnit.SECONDS;
        /**
         * 续约定时任务
         */
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        /**
         * 当前线程 - 判断锁重入
         */
        Thread currentThread;
        /**
         * 业务锁key
         */
        String lockKey;
        /**
         * etcd锁结果
         */
        ByteSequence lockPath;
        /**
         * 锁重入次数
         */
        AtomicInteger lockCount = new AtomicInteger(0);
    }

}
