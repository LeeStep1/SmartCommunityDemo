package cn.bit.trade.manager;

import cn.bit.facade.exception.trade.TradeException;
import cn.bit.facade.model.trade.Trade;
import cn.bit.facade.model.trade.TradeAccount;
import cn.bit.facade.vo.trade.Order;
import cn.bit.facade.vo.trade.TradeOrder;

/**
 * 交易流程管理器
 *
 * @author jianming.fan
 * @date 2018-10-09
 */
public interface TradeManager {

    Order createOrder(TradeAccount tradeAccount, TradeOrder tradeOrder) throws TradeException;

    Trade paymentNotify(String notifyData, TradeAccountSelector selector) throws TradeException;

    String notifyResponse();

    String getName();

    interface TradeAccountSelector {

        TradeAccount select(String appId) throws TradeException;

    }

}
