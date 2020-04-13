package cn.bit.fees.dao.Impl;

import cn.bit.facade.model.fees.PropertyBill;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
public class PropertyBillRepositoryImpl extends AbstractMongoDao<PropertyBill, ObjectId> implements
        MongoDao<PropertyBill, ObjectId>
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

}
