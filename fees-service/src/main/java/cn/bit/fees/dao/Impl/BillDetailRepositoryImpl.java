package cn.bit.fees.dao.Impl;

import cn.bit.facade.model.fees.BillDetail;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class BillDetailRepositoryImpl extends AbstractMongoDao<BillDetail, ObjectId>
        implements MongoDao<BillDetail, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
