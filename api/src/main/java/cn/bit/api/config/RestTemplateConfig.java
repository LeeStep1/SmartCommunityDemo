package cn.bit.api.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *  定义restTemplate的配置
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @ConditionalOnMissingBean({ RestOperations.class, RestTemplate.class })
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);

        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
        while (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            if (converter instanceof StringHttpMessageConverter
                    || converter instanceof MappingJackson2HttpMessageConverter) {
                iterator.remove();
            }
        }
        // 使用 utf-8 编码集的 converter 替换默认的 converter（默认的 string converter 的编码集为"ISO-8859-1"）
        messageConverters.add(new StringHttpMessageConverter(UTF_8));
        // 使用 FastJsonHttpMessageConverter4 替换默认的 MappingJackson2HttpMessageConverter
        messageConverters.add(new FastJsonHttpMessageConverter4());

        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean({ClientHttpRequestFactory.class})
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        // 创建连接池管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(50);
        connectionManager.setDefaultMaxPerRoute(50);
        // 20秒不存在活动，需要检测连接是否可用
        connectionManager.setValidateAfterInactivity(20000);

        // 创建请求重试处理器，不做任何重试
        HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);

        // 创建http client，设置连接管理器和重试处理器
        HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager)
                .setRetryHandler(retryHandler).build();

        // 创建请求工厂，设置连接超时时间和读写超时时间
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);

        return factory;
    }

}
