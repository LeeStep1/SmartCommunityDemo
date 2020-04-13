package cn.bit.business.dao.Impl;

import cn.bit.facade.model.business.Convenience;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by fxiao
 * on 2018/4/4
 */
public class ConvenienceRepositoryImpl extends AbstractMongoDao<Convenience, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

}
