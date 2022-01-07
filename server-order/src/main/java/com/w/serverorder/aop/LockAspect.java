package com.w.serverorder.aop;

import com.w.serverorder.annotation.DistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Method;

//@Component
@Aspect
public class LockAspect {
    private final WebApplicationContext context;

    @Autowired
    @Qualifier("redissonClient")
    RedissonClient redissonClient;

    public LockAspect(WebApplicationContext context) {
        this.context = context;
    }

    @Pointcut("@annotation(com.w.serverorder.annotation.DistributedLock)")
    private void methodPointCut() {
    }

    @Around("methodPointCut()")
    private Object lockAround(ProceedingJoinPoint joinPoint) {
        // 这里实现锁逻辑
        RLock lock = null;
        try {
            String lockKey = getLockKey(joinPoint);
            lock = redissonClient.getLock(lockKey);
            lock.lock();
            // 调用原始方法
            return joinPoint.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            assert lock != null;
            lock.unlock();
        }

        return null;
    }

    private String getLockKey(ProceedingJoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法
        Method method = signature.getMethod();
        // 获取参数的值
        Object[] args = joinPoint.getArgs();

        // 获取方法的注解
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);
        if (annotation.keyPrefix().equals("")) {
            throw new RuntimeException("DistributedLock 注解必须指定keyPrefix");
        }

        // 锁的键
        String lockKey = annotation.keyPrefix();

        if (!annotation.fieldName().equals("")) {
            // 如果没有字段名，就是锁整个方法
            // 如果指定字段名，锁的就是动态参数的值

            // todo 缓存（Map），缓存已解析过的方法名等
            // 解析方法的参数的名字
            DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
            String[] parameterNames = discoverer.getParameterNames(method);

            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    // 得到方法名
                    String parameterName = parameterNames[i];
                    if (parameterName.equals(annotation.fieldName())) {
                        String lockKeyValue = args[i].toString();
                        // 修改锁的键名
                        lockKey += lockKeyValue;
                        break;
                    }
                }
            }
        }

        return lockKey;
    }
}
