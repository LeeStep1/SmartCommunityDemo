package cn.bit.user;

import cn.bit.framework.redis.RedisTemplateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by terry on 2018/1/14.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class RedisTest {

    @Test
    public void test(){
        RedisTemplateUtil.set("xxx","123");
    }
}
