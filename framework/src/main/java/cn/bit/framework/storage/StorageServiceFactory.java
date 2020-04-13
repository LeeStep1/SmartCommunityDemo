package cn.bit.framework.storage;/**
 * Created by terry on 2016/8/25.
 */

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author terry
 * @create 2016-08-25 18:00
 **/
public class StorageServiceFactory implements ApplicationContextAware, InitializingBean {

    private static final Map<String, StorageService> serviceMap = new HashMap<>();
    private ApplicationContext ctx;
    private static StorageService defaultService;

    public static StorageService getService(String name) {
        return serviceMap.get(name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ctx.getBeansOfType(StorageService.class).values().forEach(service -> serviceMap.put(service.getName(),
                service));
    }

    public static StorageService getDefaultService() {
        return defaultService;
    }

    public void setDefaultService(StorageService defaultService) {
        StorageServiceFactory.defaultService = defaultService;
    }
}
