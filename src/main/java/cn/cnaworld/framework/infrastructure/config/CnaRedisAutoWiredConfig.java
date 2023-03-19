package cn.cnaworld.framework.infrastructure.config;

import cn.cnaworld.framework.infrastructure.config.aop.InterceptorConfig;
import cn.cnaworld.framework.infrastructure.config.redisson.RedissonConfig;
import cn.cnaworld.framework.infrastructure.properties.CnaworldRedisProperties;
import cn.cnaworld.framework.infrastructure.utils.redis.CnaRedisUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自动装配类
 * @author Lucifer
 * @date 2023/2/10
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties({CnaworldRedisProperties.class})
@Import(value = {CnaRedisUtil.class, RedissonConfig.class, InterceptorConfig.class})
public class CnaRedisAutoWiredConfig {}

