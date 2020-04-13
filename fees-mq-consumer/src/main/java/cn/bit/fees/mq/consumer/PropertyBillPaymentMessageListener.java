package cn.bit.fees.mq.consumer;

import cn.bit.facade.enums.TradeStatusType;
import cn.bit.facade.model.trade.Trade;
import cn.bit.facade.service.fees.FeesFacade;
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

@Component
@Slf4j
public class PropertyBillPaymentMessageListener implements MessageListenerConcurrently {

    @Autowired
    private FeesFacade feesFacade;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            Trade trade = JSON.parseObject(msg.getBody(), Trade.class);
            try {
                // 处理业务订单
                if (trade.getStatus() == TradeStatusType.PAID.value()) {
                    // 支付成功，则更新订单的状态为‘已缴费’
                    feesFacade.finishedPaymentByTradeId(trade.getId());
                } else {
                    // 支付不成功需要清理旧的交易订单流水号
                    feesFacade.clearTradeIdByTradeId(trade.getId());
                }
            } catch (Exception e) {
                // 非业务异常（网络异常，数据库异常等），no ack，需要重试
                if (!(e instanceof BizException)) {
                    log.error("处理物业账单支付回调时出现异常。", e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                // 业务异常，物业账单支付回调数据非法，ack，移除消息
                log.warn("无效的物业账单支付回调。", e);
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
