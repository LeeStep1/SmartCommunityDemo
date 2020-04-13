package cn.bit.framework.config;

import cn.bit.framework.context.ApplicationContextAware;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2018/1/4 0004.
 */
@Component
@Data
public class AppConfig {

    private static AppConfig appConfig = null;

    public static AppConfig getInstance() {
        if (appConfig == null) {
            synchronized (AppConfig.class) {
                if (appConfig == null) {
                    appConfig = ApplicationContextAware.getApplicationContext().getBean(AppConfig.class);
                }
            }
        }
        return appConfig;
    }

    //@Value("${db_type}")
    private String dbType = "1";
}
