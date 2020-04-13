package cn.bit.user.mq;

import cn.bit.common.facade.company.message.CompanyCommunityBindingMessage;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.framework.exceptions.BizException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 企业解绑社区
 */
@Component
@Slf4j
public class CompanyCommunityUnbindMessageListener implements MessageListenerConcurrently {

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                CompanyCommunityBindingMessage unbindMessage = JSON.parseObject(msg.getBody(), CompanyCommunityBindingMessage.class);
                log.info("企业解绑社区 unbindMessage: {}", unbindMessage);
                userToPropertyFacade.unbindCompany(unbindMessage.getPartner(), unbindMessage.getCompanyId(),
                        unbindMessage.getCommunityId());
            } catch (Exception e) {
                // 非业务异常（网络异常，数据库异常等），no ack，需要重试
                if (!(e instanceof BizException)) {
                    log.error("企业解绑社区消息消费失败，出现异常：{}", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，企业解绑社区数据非法，ack，移除消息
                log.warn("企业解绑社区数据非法：{}", e);
            }
        }
        log.info("企业解绑社区消息消费完成");
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
