package cn.cnaworld.framework.infrastructure.config.redisson;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author Lucifer
 * @date 2023/3/2
 * @since 1.0.0
 */
@ConditionalOnProperty(name="cnaworld.redis.enable",havingValue="false")
public class RedissonConfig {

    /**
     * 用空实现覆盖redisson默认加载配置，目的为使用cnaworld.redis.enable开关取消加载
     * @author Lucifer
     * @date 2023/3/3
     * @since 1.0.0
     * @return null
     */
    @Bean
    public RedissonClient redissonClient(){
        return null;
    }

}
