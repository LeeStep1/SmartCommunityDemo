package cn.bit.communityIoT.mq.consumer;

import cn.bit.facade.vo.mq.DoorAuthVO;
import cn.bit.framework.exceptions.BizException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

@Slf4j
public abstract class AbstractDoorAuthMessageListener implements MessageListenerConcurrently {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : messages) {
            try {
                DoorAuthVO deviceAuthVO = JSON.parseObject(msg.getBody(), DoorAuthVO.class);
                log.info("门禁授权队列接收消息" + JSON.toJSONString(deviceAuthVO));
                this.execute(deviceAuthVO);
            } catch (Exception e) {
                if (!(e instanceof BizException)) {
                    log.error("编辑门禁权限消息消费失败，出现异常：{}", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，移除消息
                log.warn("编辑门禁权限数据非法：{}", e);
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 门禁授权操作具体实现
     * @param deviceAuthVO
     * @throws Exception
     */
    protected abstract void execute(DoorAuthVO deviceAuthVO) throws Exception;
}
