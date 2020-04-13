package cn.bit.framework.utils.mq.convert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;

import java.nio.charset.Charset;

/**
 * Created by terry on 2016/7/12.
 */
public class FastJsonMessageConverter extends AbstractMessageConverter {


    private static Logger log = LoggerFactory.getLogger(FastJsonMessageConverter.class);

    public static final String DEFAULT_CHARSET = "UTF-8";

    private volatile String defaultCharset = DEFAULT_CHARSET;

    public FastJsonMessageConverter() {
        super(new MimeType("application", "json", Charset.forName("UTF-8")));
    }


    @Override
    protected boolean supports(Class<?> clazz) {
        return false;
    }
}
