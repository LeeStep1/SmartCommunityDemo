package cn.bit.framework.redis.Serializer;/**
 * Created by terry on 2016/12/15.
 */

import org.apache.log4j.Logger;
import org.nustaq.serialization.FSTConfiguration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;


/**
 * @author terry
 * @create 2016-12-15 15:40
 **/
public class FstRedisSerializer<T> implements RedisSerializer<T> {

    private static Logger logger = Logger.getLogger(FstSerializeUtils.class);
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    @Override
    public byte[] serialize(T t) throws SerializationException {
        return conf.asByteArray(t);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return (T) conf.getObjectInput(bytes).readObject();
        } catch (Exception e) {
            throw new SerializationException("Cannot deserialize", e);
        }
    }
}
