package cn.bit.framework.redis.cluster;

import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisClusterConfiguration;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: terry
 * Date: 2017/4/21
 * Description: 集群配置
 *
 #  redis 集群配置
 *  redis.maxRedirections=6
 *  redis.clusterHosts=120.76.137.184:6379,120.76.137.184:6380,120.24.234.38:6379,120.24.234.38:6380,120.24.65.223:6379,120.24.65.223:6380
 */
@Slf4j
public class RedisClusterConfigurationFactoryBean implements FactoryBean<RedisClusterConfiguration>, InitializingBean {
    private String clusterHosts;//ip:port

    private RedisClusterConfiguration redisClusterConfiguration;

//    private Integer timeout;

    private Integer maxRedirections;

    private Pattern p = Pattern.compile("^.+[:]\\d{1,5}\\s*$");

    @Override
    public RedisClusterConfiguration getObject() {
        return redisClusterConfiguration;
    }

    @Override
    public Class<? extends RedisClusterConfiguration> getObjectType() {
        return (this.redisClusterConfiguration != null ? this.redisClusterConfiguration.getClass() : RedisClusterConfiguration.class);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private Set<String> parseHostAndPort() {
        if(clusterHosts == null || clusterHosts.trim().isEmpty()){
            log.error("jedis clusterHosts[ip:port] 配置不能为空");
            throw new IllegalArgumentException("jedis clusterHosts[ip:port] 配置不能为空");
        }

        String[] split = clusterHosts.split(",");
        final Set<String> sentinelHosts = new HashSet<>();
        for (String v : split) {
            if (StringUtil.isNotBlank(v)) {
                sentinelHosts.add(v.trim());
            }
        }
        if (sentinelHosts.isEmpty()) {
            log.error("jedis clusterHosts[ip:port] 配置不能为空");
            throw new IllegalArgumentException("jedis clusterHosts[ip:port] 配置不能为空");
        }

        for (String host : sentinelHosts) {
            boolean isIpPort = p.matcher(host).matches();
            if (!isIpPort) {
                log.error("解析 jedis clusterHosts[ip 或 port] 不合法");
                throw new IllegalArgumentException("解析 jedis clusterHosts[ip 或 port] 不合法");
            }
        }
        return sentinelHosts;
    }

    @Override
    public void afterPropertiesSet() {
        Set<String> haps = this.parseHostAndPort();
        redisClusterConfiguration = new RedisClusterConfiguration(haps);
        redisClusterConfiguration.setMaxRedirects(maxRedirections);
    }

    public void setClusterHosts(String clusterHosts) {
        this.clusterHosts = clusterHosts;
    }

    /*public void setTimeout(int timeout) {
        this.timeout = timeout;
    }*/

    public void setMaxRedirections(int maxRedirections) {
        this.maxRedirections = maxRedirections;
    }
}