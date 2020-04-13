package cn.bit.system.dao.impl;

import cn.bit.facade.model.system.Client;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class ClientRepositoryImpl extends AbstractMongoDao<Client, ObjectId> implements MongoDao<Client, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

}
