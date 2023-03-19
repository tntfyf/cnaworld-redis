package cn.cnaworld.framework.infrastructure.common;

import cn.cnaworld.framework.infrastructure.utils.CnaLogUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lucifer
 * @date 2023/3/19
 * @since 1.0.5
 */
@Slf4j
public class ExceptionCallBack {


    /**
     * 异常处理回调
     * @author Lucifer
     * @date 2023/3/19
     * @since 1.0.0
     */
    public void callback(String declaringName,String redisKey,String action,Exception e){
        CnaLogUtil.error(log,"方法：{} , redisKey ：{} ,动作 : {} ,分布式锁定失败" , declaringName, redisKey,action,e);
    }
}
