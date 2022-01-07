package com.w.serverorder.lock;

import org.apache.commons.lang.NotImplementedException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public abstract class AbstractLock implements Lock {
//    void lock();
//    void unlock();


    @Override
    public abstract void lock();

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new NotImplementedException("lockInterruptibly 未实现");
    }

    @Override
    public boolean tryLock() {
        throw new NotImplementedException("tryLock 未实现");
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new NotImplementedException("tryLock 未实现");
    }

    @Override
    public abstract void unlock();

    @Override
    public Condition newCondition() {
        return null;
    }
}
