package cn.bit.trade.dao;

import cn.bit.facade.model.trade.Trade;
import cn.bit.framework.data.mongodb.MongoDao;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TradeRepository extends MongoDao<Trade, Long>, MongoRepository<Trade, Long> {

    Trade updateByIdAndTitleAndTotalAmountAndStatus(Trade trade, Long id, String title, Long totalAmount,
                                                    Integer status);

    Trade updateByIdAndPlatformAndAppIdAndPartnerIdAndTotalAmountAndStatus(Trade trade, Long id, Integer platform,
                                                                           String appId, String partnerId,
                                                                           Long totalAmount, Integer status);

    Trade findByIdAndPlatformAndAppIdAndPartnerIdAndTotalAmountAndStatus(Long id, Integer platform, String appId,
                                                                         String partnerId, Long totalAmount,
                                                                         Integer status);

}
