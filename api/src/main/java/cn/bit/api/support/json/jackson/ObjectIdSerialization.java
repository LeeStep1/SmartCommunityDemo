package cn.bit.api.support.json.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 提供Jackson对ObjectId序列化与反序列化的支持
 *
 * @author jianming.fan
 * @date 2018-08-03
 */
@JsonComponent
public class ObjectIdSerialization {

    public static class Serializer extends JsonSerializer<ObjectId> {
        @Override
        public void serialize(ObjectId value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeString(value.toString());
        }
    }

    public static class Deserializer extends JsonDeserializer<ObjectId> {
        @Override
        public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return parse(jsonParser.getValueAsString());
        }

        private ObjectId parse(String string) {
            return StringUtils.isEmpty(string) || "null".equalsIgnoreCase(string) ? null : new ObjectId(string);
        }
    }

}
