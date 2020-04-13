package cn.bit.communityIoT.mq.consumer;

import cn.bit.communityIoT.support.processor.DoorAuthProcessor;
import cn.bit.facade.vo.mq.DoorAuthVO;
import cn.bit.facade.vo.mq.ThirdPartInfoCallbackVO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.bit.facade.constant.mq.TagConstant.CALLBACK;
import static cn.bit.facade.constant.mq.TopicConstant.TOPIC_HOUSEHOLD_NEW_DOOR_AUTH;

@Component
@Slf4j
public class DoorAuthAddMessageListener extends AbstractDoorAuthMessageListener {
    @Autowired
    private List<DoorAuthProcessor> processors;

    @Autowired
    private DefaultMQProducer authCallbackProducer;

    @Override
    protected void execute(DoorAuthVO doorAuthVO) throws Exception {
        for (DoorAuthProcessor processor : processors) {
            log.info("门禁权限增加");
            ThirdPartInfoCallbackVO callbackVO = processor.add(doorAuthVO);
            if (callbackVO == null || CollectionUtils.isEmpty(callbackVO.getUserIds())) {
                log.info("add auth callbackVO is null or callbackVO.getUserIds() is empty continue. callbackVO:{}", callbackVO);
                continue;
            }
            Message message = new Message(TOPIC_HOUSEHOLD_NEW_DOOR_AUTH, CALLBACK, JSON.toJSONString(callbackVO).getBytes());
            authCallbackProducer.send(message);
        }
    }
}
