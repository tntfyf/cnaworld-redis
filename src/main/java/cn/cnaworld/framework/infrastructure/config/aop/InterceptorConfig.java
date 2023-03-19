package cn.cnaworld.framework.infrastructure.config.aop;

import cn.cnaworld.framework.infrastructure.common.RedisLockInterceptor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;

/**
 * @author Lucifer
 * @date 2023/3/18
 * @since 1.0.5
 */
@ConditionalOnExpression("#{environment['cnaworld.redis.enabled'] ==null || !environment['cnaworld.redis.enabled'].contains('false')}")
public class InterceptorConfig {

    public static final String EXECUTION = "@annotation(cn.cnaworld.framework.infrastructure.annotation.CnaRedisLock)";

    @Bean
    public DefaultPointcutAdvisor defaultPointcutAdvisor(){
        RedisLockInterceptor redisLockInterceptor = new RedisLockInterceptor();
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(EXECUTION);
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice(redisLockInterceptor);
        return advisor;
    }

}
