package cn.bit.api.support.json.jackson;

import cn.bit.framework.utils.DateUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jsoup.helper.StringUtil;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 提供Jackson对Date序列化与反序列化的支持
 *
 * @author decai.liu
 * @date 2019-04-24
 */
@JsonComponent
public class DateSerialization {

    public static class Serializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeNumber(value.getTime());
        }
    }

    public static class Deserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return parse(jsonParser.getValueAsString());
        }

        private Date parse(String string) {
            return getDateByString(string);
        }
    }

    /**
     * 通过字符串获取时间
     * @param str 时间字符串
     * @return
     */
    private static Date getDateByString(String str) {
        if (StringUtil.isBlank(str) || "null".equalsIgnoreCase(str)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(DateUtils.DATE_FORMAT_DATETIME);
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2019/02/29会被接受，并转换成2019/03/01
            format.setLenient(false);
            return format.parse(str);
        } catch (ParseException e) {
        }
        format = new SimpleDateFormat(DateUtils.DATE_FORMAT_DATEONLY);
        try {
            format.setLenient(false);
            return format.parse(str);
        } catch (ParseException e) {
        }

        return new Date(Long.parseLong(str));
    }

}
