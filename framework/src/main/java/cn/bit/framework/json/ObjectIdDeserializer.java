package cn.bit.framework.json;

import cn.bit.framework.utils.string.StringUtil;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 实现fastjson及jackson的ObjectId反序列化器
 *
 * @author jianming.fan
 */
public class ObjectIdDeserializer extends StdDeserializer<ObjectId> implements ObjectDeserializer {

    public static final ObjectIdDeserializer instance = new ObjectIdDeserializer();

    public ObjectIdDeserializer() {
        super(ObjectId.class);
    }

    @Override
    public ObjectId deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
        if (type != ObjectId.class) {
            return null;
        }

        return parse(defaultJSONParser.parseObject(String.class));
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

    @Override
    public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return parse(jsonParser.getValueAsString());
    }

    private ObjectId parse(String string) {
        return StringUtil.isBlank(string) ? null : new ObjectId(string);
    }
}
