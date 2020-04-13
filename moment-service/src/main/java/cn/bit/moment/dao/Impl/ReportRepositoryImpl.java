package cn.bit.moment.dao.Impl;

import cn.bit.facade.model.moment.Report;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
public class ReportRepositoryImpl
        extends AbstractMongoDao<Report, ObjectId> implements MongoDao<Report, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

}
