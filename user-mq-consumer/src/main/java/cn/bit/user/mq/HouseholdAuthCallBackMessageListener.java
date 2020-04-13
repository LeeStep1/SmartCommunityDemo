package cn.bit.user.mq;

import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.mq.ThirdPartInfoCallbackVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.exceptions.BizException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HouseholdAuthCallBackMessageListener implements MessageListenerConcurrently {

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                ThirdPartInfoCallbackVO thirdPartInfoCallbackVO = JSON.parseObject(msg.getBody(), ThirdPartInfoCallbackVO.class);
                log.info("回调队列接收消息" + JSON.toJSONString(thirdPartInfoCallbackVO));
                Set<String> ids = new HashSet<>(thirdPartInfoCallbackVO.getUserIds());

                UserToRoom userToRoom = userToRoomFacade.findById(thirdPartInfoCallbackVO.getCorrelationId());
                if (ids.size() > 0 && userToRoom != null) {
                    userToRoomFacade.updateMiliUIdById(userToRoom,
                            Long.valueOf(thirdPartInfoCallbackVO.getUserIds().stream().findFirst().get()));
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                UserToProperty userToProperty = userToPropertyFacade.findByIdAndCommunityId(
                        thirdPartInfoCallbackVO.getCorrelationId(), thirdPartInfoCallbackVO.getCommunityId());
                if (ids.size() > 0 && userToProperty != null) {
                    userToProperty.setMiliUIds(thirdPartInfoCallbackVO.getUserIds().stream()
                            .map(Long::valueOf).collect(Collectors.toSet()));
                    userToPropertyFacade.updateMiliUIds(userToProperty);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }


            } catch (Exception e) {
                // 非业务异常（网络异常，数据库异常等），no ack，需要重试
                if (!(e instanceof BizException)) {
                    log.error("住户权限回调消息消费失败，出现异常：{}", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，住户权限回调数据非法，ack，移除消息
                log.warn("住户权限回调数据非法：{}", e);
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
