package cn.bit.communityIoT.mq.consumer;

import cn.bit.facade.vo.mq.CreateRecordRequest;
import cn.bit.framework.exceptions.BizException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * Created by xiaoxi.lao
 *
 * @Description :
 * @Date ： 2019/9/12 11:40
 */
@Slf4j
public abstract class DeviceRecordConsumer implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                    ConsumeConcurrentlyContext context) {
        for (MessageExt msg : messages) {
            CreateRecordRequest createRecordRequest = loadMsg(msg);

            log.info("设备上传记录 : {}", createRecordRequest);
            try {
                this.writeRecord(createRecordRequest);
            } catch (Exception e) {
                if (!(e instanceof BizException)) {
                    log.error("设备记录消息消费失败，出现异常：{}", e.getMessage());
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，移除消息
                log.warn("设备记录数据非法：{}", e.getMessage());
            }
        }

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    private CreateRecordRequest loadMsg(MessageExt msg) {
        CreateRecordRequest createRecordRequest = JSON.parseObject(msg.getBody(), CreateRecordRequest.class);
        // 终端号（硬件设备编号），对应 mq的key
        createRecordRequest.setTerminalCode(msg.getKeys());
        return createRecordRequest;
    }

    /**
     * 设备记录具体实现
     * @param createRecordRequest
     */
    protected abstract void writeRecord(CreateRecordRequest createRecordRequest) throws Exception;
}
