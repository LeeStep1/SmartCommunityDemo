package cn.bit.trade.mq;

import cn.bit.facade.enums.PlatformType;
import cn.bit.facade.vo.mq.PayVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static cn.bit.facade.constant.mq.QueueConstant.QUEUE_TRADE_PAYMENT_NOTIFY;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class RabbitTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void test() {
        PayVO payVO = new PayVO();
        payVO.setPlatform(PlatformType.WECHAT.value());
        payVO.setNotifyData("abc");
        amqpTemplate.convertAndSend(QUEUE_TRADE_PAYMENT_NOTIFY, payVO);
    }

}
