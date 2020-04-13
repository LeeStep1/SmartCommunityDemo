package cn.bit.trade.listener;

import cn.bit.facade.enums.TradeStatusType;
import cn.bit.facade.model.trade.Trade;
import cn.bit.trade.dao.TradeRepository;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import javax.annotation.Resource;

/**
 * 交易事务监听器
 *
 * @author jianming.fan
 * @date 2018-10-10
 */
public class TradeTransactionListener implements TransactionListener {

    @Resource
    private TradeRepository tradeRepository;

    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {
        Trade trade = JSON.parseObject(message.getBody(), Trade.class);
        trade = tradeRepository.updateByIdAndPlatformAndAppIdAndPartnerIdAndTotalAmountAndStatus(trade, trade.getId(),
                trade.getPlatform(), trade.getAppId(), trade.getPartnerId(), trade.getTotalAmount(),
                TradeStatusType.NOT_PAY.value());
        return trade != null ? LocalTransactionState.COMMIT_MESSAGE : LocalTransactionState.ROLLBACK_MESSAGE;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        Trade tradeNotify = JSON.parseObject(messageExt.getBody(), Trade.class);
        Trade trade = tradeRepository.findById(tradeNotify.getId());
        boolean confirmed = tradeNotify.getAgtTradeNo().equals(trade.getAgtTradeNo())
                && tradeNotify.getPayAt().equals(trade.getPayAt())
                && tradeNotify.getStatus().equals(trade.getStatus());
        return confirmed ? LocalTransactionState.COMMIT_MESSAGE
                : trade.getStatus().equals(TradeStatusType.NOT_PAY.value())
                ? LocalTransactionState.UNKNOW
                : LocalTransactionState.ROLLBACK_MESSAGE;
    }
}
