package cn.bit.trade.dao.impl;

import cn.bit.facade.model.trade.Trade;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class TradeRepositoryImpl extends AbstractMongoDao<Trade, Long> implements MongoDao<Trade, Long> {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
