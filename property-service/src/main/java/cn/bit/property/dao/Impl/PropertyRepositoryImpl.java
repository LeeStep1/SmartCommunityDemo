package cn.bit.property.dao.Impl;

import cn.bit.facade.vo.property.Property;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class PropertyRepositoryImpl extends AbstractMongoDao<Property, ObjectId> implements MongoDao<Property, ObjectId>
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
