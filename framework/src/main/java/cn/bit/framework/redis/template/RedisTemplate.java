package cn.bit.framework.redis.template;

import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.redis.Serializer.FstRedisSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qiujingwang
 * Date: 2017/4/10
 * Description:
 */
public class RedisTemplate<K, V> extends org.springframework.data.redis.core.RedisTemplate implements InitializingBean {

    public RedisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();

        setKeySerializer(stringSerializer);
        setHashKeySerializer(stringSerializer);

        FstRedisSerializer fstRedisSerializer = new FstRedisSerializer();

        setHashValueSerializer(fstRedisSerializer);
        setValueSerializer(fstRedisSerializer);
    }

    /**
     * 是否启动RedisTemplateUtil工具类
     *
     * @param enable
     */
    public void setEnableRedisTemplateUtil(boolean enable) {
        if (enable) {
            RedisTemplate oThis = this;
            RedisTemplateUtil.redisTemplate(oThis);
        }
    }

    public Map<byte[], byte[]> entries(String mapKey) {
        final byte[] rawKey = rawKey(mapKey);

        return (Map<byte[], byte[]>) execute(new RedisCallback<Map<byte[], byte[]>>() {
            @Override
            public Map<byte[], byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.hGetAll(rawKey);
            }
        });
    }

    private byte[] rawKey(Object key) {
        Assert.notNull(key, "non null key required");
        if (getKeySerializer() == null && key instanceof byte[]) {
            return (byte[]) key;
        }
        return getKeySerializer().serialize(key);
    }
}
