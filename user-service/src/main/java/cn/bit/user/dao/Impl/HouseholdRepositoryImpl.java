package cn.bit.user.dao.Impl;

import cn.bit.facade.model.user.Household;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class HouseholdRepositoryImpl extends AbstractMongoDao<Household, ObjectId>
        implements MongoDao<Household, ObjectId> {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
