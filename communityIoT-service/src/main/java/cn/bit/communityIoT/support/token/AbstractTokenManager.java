package cn.bit.communityIoT.support.token;

import cn.bit.facade.service.communityIoT.TokenManager;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : xiaoxi.lao
 * @Description : 第三方设备token统一管理
 * @Date ： 2018/9/17 10:17
 */
@Slf4j
public abstract class AbstractTokenManager implements TokenManager {

    @Override
    public String refreshToken() {
        if (!lock()) {
            while (RedisTemplateUtil.hasKey(getRefreshingKey())) {
                Thread.yield();
            }
            return getTokenFromRedis();
        }

        try {
            return doRefreshToken();
        } finally {
            unlock();
        }
    }

    public String updateToken(String oldToken) {
        String cacheOldToken = RedisTemplateUtil.getAndSetString(getLastToken(), oldToken);
        if (cacheOldToken == null || !cacheOldToken.equals(oldToken)) {
            return refreshToken();
        }

        while (RedisTemplateUtil.hasKey(getRefreshingKey())) {
            Thread.yield();
        }

        return getTokenFromRedis();
    }

    public String getToken() {
        String token = getTokenFromRedis();

        if (StringUtil.isEmpty(token)) {
            return refreshToken();
        }

        // 判断redis的最新token是否被设置为过期的token
        if (token.equals(RedisTemplateUtil.getStr(getLastToken()))) {
            while (getTokenFromRedis().equals(RedisTemplateUtil.getStr(getLastToken()))) {
                Thread.yield();
            }
            return getTokenFromRedis();
        }
        return token;
    }

    private boolean lock() {
        return RedisTemplateUtil.setIfAbsent(getRefreshingKey(), String.valueOf(System.currentTimeMillis()));
    }

    private void unlock() {
        RedisTemplateUtil.del(getRefreshingKey());
    }

    /**
     * 刷新token
     * @return
     */
    protected abstract String doRefreshToken();

    protected abstract String getTokenFromRedis();

    protected abstract String getLastToken();

    protected abstract String getRefreshingKey();
}
