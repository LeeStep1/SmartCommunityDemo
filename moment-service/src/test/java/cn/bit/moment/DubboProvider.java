package cn.bit.moment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by decai.liu on 2018/4/13
 */
public class DubboProvider
{
    private static final Logger log = LoggerFactory.getLogger(DubboProvider.class);
    //ConnectionFactory
    public static void main(String[] args) {
        try {
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/spring-context.xml");
            context.start();
        } catch (Exception e) {
            log.error("== DubboProvider context start error:",e);
        }
        synchronized (DubboProvider.class) {
            while (true) {
                try {
                    DubboProvider.class.wait();
                } catch (InterruptedException e) {
                    log.error("== synchronized error:",e);
                }
            }
        }
    }
}