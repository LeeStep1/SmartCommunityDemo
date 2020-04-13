package cn.bit.communityIoT.dao.Impl;

import cn.bit.facade.model.communityIoT.Camera;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class CameraRepositoryImpl extends AbstractMongoDao<Camera, ObjectId> implements MongoDao<Camera, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
