package cn.bit.facade.service.trade;

import cn.bit.facade.exception.trade.TradeException;
import cn.bit.facade.model.trade.Trade;
import cn.bit.facade.model.trade.TradeAccount;
import cn.bit.facade.vo.mq.PayVO;
import cn.bit.facade.vo.trade.Notification;
import cn.bit.facade.vo.trade.Order;
import cn.bit.facade.vo.trade.TradeOrder;
import cn.bit.facade.vo.trade.TradeQueryRequest;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;

public interface TradeFacade {

    /**
     * 增加交易账号
     *
     * @param tradeAccount
     * @return
     */
    TradeAccount addTradeAccount(TradeAccount tradeAccount) throws TradeException;

    /**
     * 更新交易账号
     *
     * @param tradeAccount
     * @return
     */
    TradeAccount updateTradeAccount(TradeAccount tradeAccount) throws TradeException;

    /**
     * 根据id获取交易账号
     *
     * @param id
     * @return
     */
    TradeAccount getTradeAccount(ObjectId id) throws TradeException;

    /**
     * 根据id删除交易账号
     *
     * @param id
     * @return
     */
    boolean deleteTradeAccount(ObjectId id) throws TradeException;

    /**
     * 根据平台分页获取交易账户
     *
     * @param platform 可为null，为null获取所有平台的交易账户
     * @param page
     * @param size
     * @return
     */
    Page<TradeAccount> getTradeAccounts(Integer platform, int page, int size) throws TradeException;

    /**
     * 根据账户id数组获取交易账户
     */
    List<TradeAccount> getTradeAccounts(Collection<ObjectId> ids) throws TradeException;

    /**
     * 创建订单
     *
     * @param tradeOrder
     * @return
     */
    Order createTrade(TradeOrder tradeOrder) throws TradeException;

    /**
     * 订单查询
     *
     * @param tradeQueryRequest
     * @return
     */
    Trade tradeQuery(TradeQueryRequest tradeQueryRequest) throws TradeException;

    /**
     * 支付回调
     *
     * @param payment
     * @return
     * @throws TradeException
     */
    String paymentNotify(Notification payment) throws TradeException;

}
