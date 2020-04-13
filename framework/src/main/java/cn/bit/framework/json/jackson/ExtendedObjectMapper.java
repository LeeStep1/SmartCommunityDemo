package cn.bit.framework.json.jackson;

import cn.bit.framework.json.ObjectIdDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public class ExtendedObjectMapper extends ObjectMapper {

    public ExtendedObjectMapper() {
        super();

        init();
    }

    private void init() {
        initSerializers();
        initDeserializers();
    }

    private void initSerializers() {
        super._serializerFactory = super._serializerFactory.withAdditionalSerializers(serializers());
    }

    private void initDeserializers() {
        DeserializerFactory df = super._deserializationContext.getFactory().withAdditionalDeserializers(deserializers());
        super._deserializationContext = super._deserializationContext.with(df);
    }

    private static Serializers serializers() {
        SimpleSerializers simpleSerializers = new SimpleSerializers();
        simpleSerializers.addSerializer(ObjectId.class, new ToStringSerializer());

        return simpleSerializers;
    }

    private static Deserializers deserializers() {
        SimpleDeserializers simpleDeserializers = new SimpleDeserializers();
        simpleDeserializers.addDeserializer(ObjectId.class, new ObjectIdDeserializer());

        return simpleDeserializers;
    }
}
