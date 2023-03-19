package cn.cnaworld.framework.infrastructure.annotation;

import cn.cnaworld.framework.infrastructure.common.ExceptionCallBack;
import cn.cnaworld.framework.infrastructure.statics.LockType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 * 若key相关参数均无配置，则默认使用方法名称
 * @author Lucifer
 * @date 2023/1/30
 * @since 1.0.5
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CnaRedisLock {

    /**
     * 方法参数作为分布式锁
     * 参数若为object类型，需重写toString方法生成可被锁定的key
     */
    String[] paramsAsKey() default {};

    /**
     * 分布式锁
     */
    String key() default "";

    /**
     * 为分布式锁类型
     */
    LockType lockType() default LockType.Lock;

    /**
     * 为分布式锁前缀
     */
    String prefix() default "";

    /**
     * 阻塞
     * 非阻塞时，获取不到锁直接返回
     */
    boolean sync () default true;

    /**
     * 最大阻塞时间默认为10S
     * -1 无最大阻塞时间限制
     */
    long waitTime () default 10;

    /**
     * 时间单位
     */
    TimeUnit timeUnit ()default TimeUnit.SECONDS;

    /**
     * 时间单位
     */
    Class<? extends ExceptionCallBack> exceptionCallBack() default ExceptionCallBack.class;

}

