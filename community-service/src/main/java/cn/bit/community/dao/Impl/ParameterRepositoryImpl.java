package cn.bit.community.dao.Impl;

import cn.bit.facade.model.community.Parameter;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class ParameterRepositoryImpl extends AbstractMongoDao<Parameter, ObjectId>
        implements MongoDao<Parameter, ObjectId> {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
