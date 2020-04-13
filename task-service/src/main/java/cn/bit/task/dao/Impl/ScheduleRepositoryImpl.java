package cn.bit.task.dao.Impl;

import cn.bit.facade.model.task.Schedule;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class ScheduleRepositoryImpl
        extends AbstractMongoDao<Schedule, ObjectId>
        implements MongoDao<Schedule, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
