package cn.bit.community.dao.Impl;

import cn.bit.facade.model.community.District;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class DistrictRepositoryImpl extends AbstractMongoDao<District, ObjectId>
        implements MongoDao<District, ObjectId> {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

}
