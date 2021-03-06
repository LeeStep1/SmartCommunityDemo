package cn.bit.push.dao.impl;

import cn.bit.facade.model.push.PushTemplate;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class PushTemplateRepositoryImpl extends AbstractMongoDao<PushTemplate, ObjectId> implements MongoDao<PushTemplate, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

}
