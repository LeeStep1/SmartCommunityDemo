package cn.bit.job;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static cn.bit.facade.constant.mq.TagConstant.ELEVATOR;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_COMMUNITYIOT_UNIVERSAL_CERTIFICATE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class QueueTest {
    @Autowired
    private DefaultMQProducer deviceAuthProducer;

    @Test
    public void execute() {
        List<ObjectId> communityList = Arrays.asList(new ObjectId("5a82adf3b06c97e0cd6c0f3d"),
                new ObjectId("5a8cfa62518089ae7afccc0c"), new ObjectId("5b208a81e4b03b09bfdc08df"));
        for (ObjectId community : communityList) {
            Message doorMessage = new Message(TOPIC_COMMUNITYIOT_UNIVERSAL_CERTIFICATE, ELEVATOR,
                    community.toHexString().getBytes());
            try {
                deviceAuthProducer.send(doorMessage);
            } catch (MQClientException | RemotingException | InterruptedException | MQBrokerException e) {
                e.printStackTrace();
            }
        }
    }
}
