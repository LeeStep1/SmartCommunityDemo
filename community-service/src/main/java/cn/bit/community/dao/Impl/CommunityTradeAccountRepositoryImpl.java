package cn.bit.community.dao.Impl;

import cn.bit.facade.model.community.CommunityTradeAccount;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class CommunityTradeAccountRepositoryImpl extends AbstractMongoDao<CommunityTradeAccount, ObjectId> implements MongoDao<CommunityTradeAccount, ObjectId> {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
