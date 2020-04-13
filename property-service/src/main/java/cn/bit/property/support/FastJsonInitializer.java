package cn.bit.property.support;

import cn.bit.framework.json.ObjectIdDeserializer;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class FastJsonInitializer implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        // 注册ObjectId序列化器
        SerializeConfig.getGlobalInstance().put(ObjectId.class, ToStringSerializer.instance);
        // 注册ObjectId反序列化器
        ParserConfig.getGlobalInstance().getDeserializers().put(ObjectId.class, ObjectIdDeserializer.instance);
    }
}
