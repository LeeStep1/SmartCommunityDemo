package cn.bit.api.config;

import cn.bit.api.support.NeteaseImService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Desc
 *
 * @author jianming.fan
 * @date 2018-12-28
 */
@Configuration
public class ImConfig {

    @Value("${im.registerUrl}")
    private String registerUrl;

    @Value("${im.updateUrl}")
    private String updateUrl;

    @Value("${im.appKey}")
    private String appKey;

    @Value("${im.appSecret}")
    private String appSecret;

    @Bean
    public NeteaseImService getNeteaseIMService() {
        return new NeteaseImService(registerUrl, updateUrl, appKey, appSecret);
    }

}
