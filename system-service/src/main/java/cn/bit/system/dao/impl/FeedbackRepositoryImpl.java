package cn.bit.system.dao.impl;

import cn.bit.facade.model.system.Feedback;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class FeedbackRepositoryImpl extends AbstractMongoDao<Feedback, ObjectId> implements MongoDao<Feedback, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
