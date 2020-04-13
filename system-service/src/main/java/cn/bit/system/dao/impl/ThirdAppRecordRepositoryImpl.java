package cn.bit.system.dao.impl;

import cn.bit.facade.model.system.ThirdApp;
import cn.bit.facade.model.system.ThirdAppRecord;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class ThirdAppRecordRepositoryImpl extends AbstractMongoDao<ThirdAppRecord, ObjectId> implements MongoDao<ThirdAppRecord, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
