package cn.cnaworld.framework.infrastructure.common;

import cn.cnaworld.framework.infrastructure.annotation.CnaRedisLock;
import cn.cnaworld.framework.infrastructure.utils.log.CnaLogUtil;
import cn.cnaworld.framework.infrastructure.utils.redis.CnaRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * @author Lucifer
 * @date 2023/3/18
 * @since 1.0.5
 */
@Slf4j
public class RedisLockInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable{

        Lock lock = null;
        CnaRedisLock annotation = null;
        String declaringName = null;
        String redisKey = null;
        try {
            Method method = invocation.getMethod();
            declaringName = method.getDeclaringClass().getName()+"."+method.getName();
            annotation = method.getAnnotation(CnaRedisLock.class);
            StringBuilder keyBuilder= new StringBuilder();
            extractedRedisKey(invocation, method, declaringName, annotation, keyBuilder);
            redisKey = keyBuilder.toString();
            lock = lock(declaringName, annotation, redisKey);
            if (lock == null){
                extractedCallBack(declaringName, annotation, redisKey,"lock",null);
                return null;
            }
        }catch (Exception e){
            extractedCallBack(declaringName, annotation, redisKey,"lock", e);
        }
        Object obj;
        try {
            obj =  invocation.proceed();
        } finally {
            try {
                lock.unlock();
                CnaLogUtil.debug(log,"方法：{} , redisKey ：{} ,解除锁定成功 " ,declaringName, redisKey);
            }catch (Exception e) {
                extractedCallBack(declaringName, annotation, redisKey,"unlock",e);
            }
        }
        return obj;
    }

    private void extractedCallBack(String declaringName, CnaRedisLock annotation, String redisKey,String action, Exception e) {
        try {
            annotation.exceptionCallBack().newInstance().callback(declaringName, redisKey,action, e);
        } catch (InstantiationException | IllegalAccessException ex) {
            CnaLogUtil.error(log,"方法：{} , redisKey ：{} ,分布式锁定 , 异常处理回调实例化异常" , declaringName, redisKey, ex,e);
        }
    }

    /**
     * 根据参数类型加锁
     */
    private Lock lock(String declaringName, CnaRedisLock annotation, String redisKey) throws InterruptedException {
        Lock lock =null;
        boolean result;
        //根据锁类型加锁
        switch (annotation.lockType()){
            case FairLock:
                lock = CnaRedisUtil.getFairLock(redisKey);
                //尝试加公平锁
                result=tryLock(declaringName, annotation, redisKey, lock);
                break;
            case Lock:
            default:
                //尝试加非公平锁
                lock = CnaRedisUtil.getLock(redisKey);
                result=tryLock(declaringName, annotation, redisKey, lock);
                break;
        }
        if (!result) {
            return null;
        }
        return lock;
    }

    /**
     * 根据注解值处理锁定的key
     */
    private void extractedRedisKey(MethodInvocation invocation, Method method, String declaringName, CnaRedisLock annotation, StringBuilder keyBuilder) {
        String prefix = annotation.prefix();
        //处理前缀
        if (StringUtils.isNotBlank(prefix)) {
            keyBuilder.append(prefix);
        }

        String key = annotation.key();
        //处理直接key
        if (StringUtils.isNotBlank(key)) {
            keyBuilder.append(key);
        }else {
            //处理入参对应值
            extractedParamsAsKeys(invocation, method, annotation, keyBuilder);
        }
        //若都没有值则默认使用方法全限定名
        if (keyBuilder.length()==0) {
            keyBuilder.append(declaringName);

        }
    }

    /**
     * 处理入参对应值
     */
    private static void extractedParamsAsKeys(MethodInvocation invocation, Method method, CnaRedisLock annotation, StringBuilder keyBuilder) {
        String[] paramsAsKeys = annotation.paramsAsKey();
        if (ObjectUtils.isNotEmpty(paramsAsKeys)) {
            Map<String,Object> argumentsMap = null;
            Parameter[] parameters = method.getParameters();
            Object[] arguments = invocation.getArguments();
            if (ObjectUtils.isNotEmpty(parameters)) {
                argumentsMap  = new HashMap<>(8);
                for (int i = 0; i < parameters.length; i++){
                    //拿到入参名称及值
                    Parameter parameter = parameters[i];
                    Object argument = arguments[i];
                    String name = parameter.getName();
                    argumentsMap.put(name,argument);
                }
            }
            //入参名称及值与注解设置比较，匹配则拼接
            if (ObjectUtils.isNotEmpty(argumentsMap)) {
                for (String paramsAsKey:paramsAsKeys) {
                    if (argumentsMap.containsKey(paramsAsKey)) {
                        Object argument = argumentsMap.get(paramsAsKey);
                        String value = String.valueOf(argument);
                        keyBuilder.append(value);
                    }
                }
            }
        }
    }

    private boolean tryLock(String declaringName, CnaRedisLock annotation, String redisKey, Lock lock) throws InterruptedException {
        boolean result = false;
        //是否阻塞
        if (annotation.sync()) {
            //阻塞最大时间大于-1则设置
            if(annotation.waitTime()>-1){
                boolean tryLock = lock.tryLock(annotation.waitTime(), annotation.timeUnit());
                result=tryLockResult(declaringName,annotation, redisKey, tryLock);
            }else {
                //阻塞最大时间大于小于等于-1则一直阻塞
                try {
                    lock.lock();
                }catch (Exception e) {
                    result=tryLockResult(declaringName,annotation, redisKey, false);
                }
            }
        }else {
            //非阻塞最大时间大于小于等于-1则一直阻塞
            boolean tryLock = lock.tryLock();
            result=tryLockResult(declaringName,annotation, redisKey, tryLock);
        }
        return result;
    }

    private boolean tryLockResult(String declaringName, CnaRedisLock annotation, String redisKey, boolean tryLock) {
        boolean result;
        //根据加锁情况处理后续操作
        if (tryLock) {
            CnaLogUtil.debug(log,"方法：{} , redisKey ：{} ,分布式锁定成功 " , declaringName, redisKey);
            result=true;
        }else {
            result=false;
        }
        return result;
    }
}
