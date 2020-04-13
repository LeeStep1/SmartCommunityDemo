package cn.bit.fees.dao.Impl;

import cn.bit.facade.model.fees.PropFeeItem;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class PropFeeItemRepositoryImpl extends AbstractMongoDao<PropFeeItem, String> implements MongoDao<PropFeeItem, String>
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
