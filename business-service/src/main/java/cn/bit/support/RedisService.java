package cn.bit.support;

import cn.bit.framework.redis.RedisTemplateUtil;
import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * redis分布式锁
 */
@Slf4j
@Component
public class RedisService {

    /***
     * 加锁
     * @param key
     * @param value 当前时间+超时时间
     * @return 锁住返回true
     */
    public static boolean lock(String key,String value){
        if(RedisTemplateUtil.setIfAbsent(key, value)){//setNX 返回boolean
            return true;
        }
        //如果锁超时 ***
        String currentValue = RedisTemplateUtil.getStr(key);
        if(!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue)<System.currentTimeMillis()){
            //获取上一个锁的时间
            String oldvalue  = RedisTemplateUtil.getAndSetString(key, value);;
            if(!StringUtils.isEmpty(oldvalue)&&oldvalue.equals(currentValue)){
                return true;
            }
        }
        return false;
    }
    /***
     * 解锁
     * @param key
     * @param value
     * @return
     */
    public static void unlock(String key,String value){
        try {
            String currentValue = RedisTemplateUtil.getStr(key);
            if(!StringUtils.isEmpty(currentValue)&&currentValue.equals(value)){
                RedisTemplateUtil.del(key);
            }
        } catch (Exception e) {
            log.error("解锁异常");
        }
    }
}
