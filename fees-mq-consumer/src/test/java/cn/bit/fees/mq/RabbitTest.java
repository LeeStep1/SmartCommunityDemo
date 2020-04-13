package cn.bit.fees.mq;

import cn.bit.facade.model.trade.Trade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class RabbitTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void test() {
        Trade trade = new Trade();
        trade.setId(1L);
        trade.setStatus(0);
        amqpTemplate.convertAndSend("property.bill.payment.notify", trade);
    }

}
