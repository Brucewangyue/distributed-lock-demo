package com.w.serverorder.lock;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 适合小项目
 * 还需优化的问题
 *  lock 中递归查询数据库的次数问题
 *  还需要一个触发器，处理超时的锁，防止加锁的过程中应用挂了
 *  假设在获取了锁以后，这个抢订单的业务执行时长超过了锁的有效期（加入按上面的方式加了锁的超时处理触发器），
 *      如果此时用订单Id去释放锁，会把别人的锁给释放
 */
@Component
@Data
public class MysqlDistributeLock extends AbstractLock {

    // todo 设计一张common_lock表来存储所有锁记录
    public ThreadLocal<Supplier<Boolean>> lockSupplier = new ThreadLocal<>();
    public ThreadLocal<Supplier<Boolean>> unLockSupplier = new ThreadLocal<>();

    @Override
    public void lock() {
        // todo 锁重入判断，增加当前线程重入次数变量

        if (tryLock()) {
            System.out.println("加锁成功!");
            return;
        }

        try {
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //todo 修改重试策略：重试一定次数后退出，增加重试次数变量
        //todo 修改重试策略：重试一定时长后退出，增加重试累计时长和重试时长阈值变量
        // 递归等待锁
        lock();
    }

    public boolean tryLock() {
        try {
            // (通过主键冲突异常)
            return lockSupplier.get().get();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void unlock() {
        unLockSupplier.get().get();
        unLockSupplier.remove();
        lockSupplier.remove();
    }
}
