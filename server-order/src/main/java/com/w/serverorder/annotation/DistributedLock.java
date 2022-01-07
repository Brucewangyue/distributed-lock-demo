package com.w.serverorder.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD})
@Retention(RUNTIME)
public @interface DistributedLock {

    /**
     * 需要锁住的key的参数名
     */
//    String value() default "";

    String keyPrefix() default "";

    String fieldName() default "";

    long expire() default 30;
}
