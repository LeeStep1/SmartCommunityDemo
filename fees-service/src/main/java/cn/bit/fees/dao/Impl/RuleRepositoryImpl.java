package cn.bit.fees.dao.Impl;

import cn.bit.facade.model.fees.Rule;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class RuleRepositoryImpl extends AbstractMongoDao<Rule, String>
        implements MongoDao<Rule, String> {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    /*@Override
    public Integer updateMultiByFeeItemId(Rule toUpdate, ObjectId feeItemId) {
        return updateMultiBy(toUpdate, feeItemId, DataStatusType.VALID.KEY);
    }*/
}
