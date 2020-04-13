package cn.bit.property.dao.Impl;

import cn.bit.facade.model.property.Registration;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class RegistrationRepositoryImpl extends AbstractMongoDao<Registration, ObjectId>
        implements MongoDao<Registration, ObjectId>
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
