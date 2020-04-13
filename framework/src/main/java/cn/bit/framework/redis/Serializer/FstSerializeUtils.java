package cn.bit.framework.redis.Serializer;/**
 * Created by terry on 2016/9/14.
 */

import org.apache.log4j.Logger;
import org.nustaq.serialization.FSTConfiguration;

/**
 * @author terry
 * @create 2016-09-14 18:15
 **/
public class FstSerializeUtils {

    private static Logger logger = Logger.getLogger(FstSerializeUtils.class);
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    public static byte[] serialize(Object object) throws Exception {
        return conf.asByteArray(object);
    }

    public static Object unSerialize(byte[] bytes) throws Exception {
        return conf.getObjectInput(bytes).readObject();
    }

}
