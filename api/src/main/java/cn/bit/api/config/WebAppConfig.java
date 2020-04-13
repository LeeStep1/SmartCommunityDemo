package cn.bit.api.config;

import cn.bit.api.support.interceptor.*;
import cn.bit.framework.json.ObjectIdDeserializer;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Created by terry on 2018/1/15.
 */
@Configuration
@EnableAutoConfiguration
public class WebAppConfig extends WebMvcConfigurerAdapter {

    /**
     * token拦截器
     *
     * @return
     */
    @Bean
    public HandlerInterceptor getTokenInterceptor() {
        return new TokenInterceptor();
    }

    @Bean
    public HandlerInterceptor getMomentInterceptor(){
        return new MomentInterceptor();
    }

    /*@Bean
    public HandlerInterceptor getThirdAppInterceptor() { return new ThirdAppInterceptor(); }*/

    @Bean
    public HandlerInterceptor getAppInterceptor() {
        return new AppInterceptor();
    }

    @Bean
    public HandlerInterceptor getHeaderInterceptor() {
        return new HeaderInterceptor();
    }

    @Bean
    public HandlerInterceptor getApiInterceptor() {
        return new ApiInterceptor();
    }

    /**
     * String 转 ObjectId
     * @return
     */
    @Bean
    public Converter<String, ObjectId> getObjectIdConverter() {
        return new Converter<String, ObjectId>() {
            @Override
            public ObjectId convert(String s) {
                return new ObjectId(s);
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用于排除拦截
        registry.addInterceptor(getAppInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/**/version/**/new")
                .excludePathPatterns("/**/zhfreeview/**");

        registry.addInterceptor(getTokenInterceptor()).addPathPatterns("/**");

        // header 拦截
        registry.addInterceptor(getHeaderInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/v1/user/add")
                .excludePathPatterns("/v1/user/signIn")
                .excludePathPatterns("/v1/user/signInByCode")
                .excludePathPatterns("/v1/user/getVerificationCode")
                .excludePathPatterns("/v1/user/resetPassword")
                .excludePathPatterns("/v1/user/**/getUser")
                .excludePathPatterns("/v1/user/wechat/phone")
                .excludePathPatterns("/v1/community/add")
                .excludePathPatterns("/v1/community/page")
                .excludePathPatterns("/v1/community/list")
                .excludePathPatterns("/v1/community/**/detail")
                .excludePathPatterns("/v1/community/**/page")
                .excludePathPatterns("/v1/community/key-value")
                .excludePathPatterns("/v1/community/**/queryByUserId")
                .excludePathPatterns("/v1/community/list-with-open")
                .excludePathPatterns("/v1/community/**/building-group")
                .excludePathPatterns("/v1/community/hierarchy")
                .excludePathPatterns("/v1/community/freeview/quickAdd")
                .excludePathPatterns("/v1/wuye/**")
                .excludePathPatterns("/v1/oss/**")
                .excludePathPatterns("/**/version/**/new")
                .excludePathPatterns("/**/zhfreeview/**")
                .excludePathPatterns("/v1/communityIoT/weather/**")
                .excludePathPatterns("/v1/biz/**");

        // api 拦截
        registry.addInterceptor(getApiInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/v1/user/signIn")
                .excludePathPatterns("/v1/user/signInByCode")
                .excludePathPatterns("/v1/user/getVerificationCode")
                .excludePathPatterns("/v1/user/wechat/phone")
                .excludePathPatterns("/v1/biz/**");

        // 拦截发布动态
        registry.addInterceptor(getMomentInterceptor())
                .addPathPatterns("/**/moment/publish")
                .addPathPatterns("/**/comment/answer");

        super.addInterceptors(registry);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        // 注册ObjectId序列化器
        SerializeConfig.getGlobalInstance().put(ObjectId.class, ToStringSerializer.instance);
        // 注册ObjectId反序列化器
        ParserConfig.getGlobalInstance().getDeserializers().put(ObjectId.class, ObjectIdDeserializer.instance);
    }
}
