package cn.bit.property.dao.Impl;

import cn.bit.facade.model.property.Alarm;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class AlarmRepositoryImpl extends AbstractMongoDao<Alarm, ObjectId>
        implements MongoDao<Alarm, ObjectId>
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
