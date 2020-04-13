package cn.bit.property.dao.Impl;

import cn.bit.facade.model.property.Gtaskzs;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by fxiao
 * on 2018/3/26
 */
public class GtaskzsRepositoryImpl extends AbstractMongoDao<Gtaskzs, ObjectId> implements MongoDao<Gtaskzs, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
