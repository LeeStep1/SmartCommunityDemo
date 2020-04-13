package cn.bit.trade.service;

import cn.bit.facade.constant.mq.TopicConstant;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.PlatformType;
import cn.bit.facade.enums.TradeBizType;
import cn.bit.facade.enums.TradeStatusType;
import cn.bit.facade.exception.trade.TradeException;
import cn.bit.facade.model.trade.Trade;
import cn.bit.facade.model.trade.TradeAccount;
import cn.bit.facade.service.trade.TradeFacade;
import cn.bit.facade.vo.trade.Notification;
import cn.bit.facade.vo.trade.Order;
import cn.bit.facade.vo.trade.TradeOrder;
import cn.bit.facade.vo.trade.TradeQueryRequest;
import cn.bit.framework.data.common.IdBuilder;
import cn.bit.framework.data.common.Page;
import cn.bit.trade.dao.TradeAccountRepository;
import cn.bit.trade.dao.TradeRepository;
import cn.bit.trade.manager.TradeManager;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.trade.TradeException.*;

@Service("tradeFacade")
@Slf4j
public class TradeFacadeImpl implements TradeFacade {
    @Resource
    private IdBuilder tradeIdBuilder;

    @Resource
    private IdBuilder refundIdBuilder;

    @Resource
    private TradeAccountRepository tradeAccountRepository;

    @Resource
    private TradeRepository tradeRepository;

    private Map<String, TradeManager> managerMap;

    @Resource
    private DefaultMQProducer mqProducer;

    @Autowired
    public TradeFacadeImpl(List<TradeManager> managers) {
        managerMap = managers.stream().collect(Collectors.toMap(TradeManager::getName, manager -> manager));
    }

    @Override
    public TradeAccount addTradeAccount(TradeAccount tradeAccount) {
        PlatformType platformType = PlatformType.fromValue(tradeAccount.getPlatform());
        if (platformType == PlatformType.UNKNOWN) {
            throw UNSUPPORTED_PLATFORM;
        }

        tradeAccount.setCreateAt(new Date());
        tradeAccount.setUpdateAt(tradeAccount.getCreateAt());
        tradeAccount.setDataStatus(DataStatusType.VALID.KEY);
        tradeAccount = tradeAccountRepository.insert(tradeAccount);

        return tradeAccount;
    }

    @Override
    public TradeAccount updateTradeAccount(TradeAccount tradeAccount) {
        tradeAccount.setUpdateAt(new Date());
        tradeAccount = tradeAccountRepository.updateOne(tradeAccount);
        if (tradeAccount == null) {
            throw ACCOUNT_NOT_EXISTS;
        }

        return tradeAccount;
    }

    @Override
    public TradeAccount getTradeAccount(ObjectId id) {
        return tradeAccountRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public boolean deleteTradeAccount(ObjectId id) {
        TradeAccount tradeAccount = new TradeAccount();
        tradeAccount.setId(id);
        tradeAccount.setDataStatus(DataStatusType.INVALID.KEY);
        tradeAccount = tradeAccountRepository.updateOne(tradeAccount);
        if (tradeAccount == null) {
            return false;
        }

        return true;
    }

    @Override
    public Page<TradeAccount> getTradeAccounts(Integer platform, int page, int size) {
        TradeAccount tradeAccount = new TradeAccount();
        tradeAccount.setPlatform(platform);
        tradeAccount.setDataStatus(DataStatusType.VALID.KEY);
        return tradeAccountRepository.findPage(tradeAccount, page, size, null);
    }

    @Override
    public List<TradeAccount> getTradeAccounts(Collection<ObjectId> ids) {
        return tradeAccountRepository.findByIdInAndDataStatus(ids, DataStatusType.VALID.KEY);
    }

    @Override
    public Order createTrade(TradeOrder tradeOrder) {
        TradeAccount tradeAccount = checkOrGetTradeAccount(tradeOrder.getTradeAccountId());
        Trade trade = checkOrSaveTrade(tradeAccount, tradeOrder);
        return doCreateTrade(tradeAccount, tradeOrder);
    }

    private Order doCreateTrade(TradeAccount tradeAccount, TradeOrder tradeOrder) {
        PlatformType platformType = PlatformType.fromValue(tradeAccount.getPlatform());
        TradeManager manager = checkOrGetManager(platformType);
        return manager.createOrder(tradeAccount, tradeOrder);
    }

    private Trade checkOrSaveTrade(TradeAccount tradeAccount, TradeOrder tradeOrder) {
        Trade trade = new Trade();
        trade.setPlatform(tradeAccount.getPlatform());
        trade.setAppId(tradeAccount.getAppId());
        trade.setPartnerId(tradeAccount.getPartnerId());
        trade.setDetail(tradeOrder.getDetail());

        if (tradeOrder.getTradeId() != null) {
            Trade updated = tradeRepository.updateByIdAndTitleAndTotalAmountAndStatus(trade,
                    tradeOrder.getTradeId(), tradeOrder.getTitle(), tradeOrder.getTotalAmount(),
                    TradeStatusType.NOT_PAY.value());
            if (updated != null) {
                return updated;
            }

            updated = tradeRepository.findById(tradeOrder.getTradeId());
            if (updated != null && !trade.getStatus().equals(TradeStatusType.NOT_PAY.value())) {
                throw TRADE_ALREADY_DONE;
            }
        }

        Long tradeId = tradeOrder.getTradeId() != null ? tradeOrder.getTradeId() : tradeIdBuilder.nextId();
        trade.setId(tradeId);
        trade.setBizType(tradeOrder.getBizType());
        trade.setTitle(tradeOrder.getTitle());
        trade.setGoodsType(tradeOrder.getGoodsType());
        trade.setUserId(tradeOrder.getUserId());
        trade.setCreateAt(new Date());
        trade.setStatus(TradeStatusType.NOT_PAY.value());
        trade.setTotalAmount(tradeOrder.getTotalAmount());
        trade.setExpireAt(tradeOrder.getExpireAt());

        tradeOrder.setTradeId(tradeId);

        return tradeRepository.insert(trade);
    }

    @Override
    public Trade tradeQuery(TradeQueryRequest tradeQueryRequest) {
        TradeAccount tradeAccount = checkOrGetTradeAccount(tradeQueryRequest.getTradeAccountId());

        PlatformType platformType = PlatformType.fromValue(tradeAccount.getPlatform());
        switch (platformType) {
            case WECHAT:
                break;
            case ALIPAY:
                break;
            default:

        }

        return null;
    }

    @Override
    public String paymentNotify(Notification payment) throws TradeException {

        PlatformType platformType = PlatformType.valueOf(payment.getPlatform().toUpperCase());

        log.info("payment platform: {}, notify data: {}", platformType, payment.getNotifyData());

        TradeManager manager = checkOrGetManager(platformType);
        Trade tradeNotify = manager.paymentNotify(payment.getNotifyData(), appId -> {
            TradeAccount tradeAccount = tradeAccountRepository.findByPlatformAndAppIdAndDataStatus(
                    platformType.value(), appId, DataStatusType.VALID.KEY);
            if (tradeAccount == null) {
                throw MISSING_TRADE_ACCOUNT;
            }

            return tradeAccount;
        });

        Trade trade = tradeRepository.findByIdAndPlatformAndAppIdAndPartnerIdAndTotalAmountAndStatus(tradeNotify.getId(),
                platformType.value(), tradeNotify.getAppId(), tradeNotify.getPartnerId(), tradeNotify.getTotalAmount(),
                TradeStatusType.NOT_PAY.value());
        if (trade != null) {
            trade.setAgtTradeNo(tradeNotify.getAgtTradeNo());
            trade.setPayAt(tradeNotify.getPayAt());
            trade.setStatus(tradeNotify.getStatus());

            Message message = new Message(TopicConstant.TOPIC_TRADE_PAYMENT,
                    TradeBizType.fromValue(trade.getBizType()).name().toLowerCase(),
                    JSON.toJSONString(trade).getBytes(StandardCharsets.UTF_8));
            try {
                mqProducer.sendMessageInTransaction(message, null);
            } catch (MQClientException e) {
                log.error("send message failed: {}", message);
            }
        }

        return manager.notifyResponse();
    }

    private TradeAccount checkOrGetTradeAccount(ObjectId tradeAccountId) throws TradeException {
        TradeAccount tradeAccount = getTradeAccount(tradeAccountId);
        if (tradeAccount == null) {
            throw ACCOUNT_NOT_EXISTS;
        }

        return tradeAccount;
    }

    private TradeManager checkOrGetManager(PlatformType platformType) throws TradeException {
        TradeManager manager = managerMap.get(platformType.name().toLowerCase());
        if (manager == null) {
            throw UNSUPPORTED_PLATFORM;
        }

        return manager;
    }

}
