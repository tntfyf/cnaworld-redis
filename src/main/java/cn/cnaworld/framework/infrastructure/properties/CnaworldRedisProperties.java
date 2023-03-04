package cn.cnaworld.framework.infrastructure.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * cnaworld属性配置
 * @author Lucifer
 * @date 2023/1/30
 * @since 1.0
 */
@ConfigurationProperties(prefix="cnaworld")
@Getter
@Setter
@ToString
public class CnaworldRedisProperties {

    private RedisProperties redis;

    /**
     * redis配置
     * @author Lucifer
     * @date 2023/1/30
     * @since 1.0
     */
    @Getter
    @Setter
    @ToString
    public static class RedisProperties {
        /**
         * 默认实现开关
         */
        private boolean enable = true;
    }

}
