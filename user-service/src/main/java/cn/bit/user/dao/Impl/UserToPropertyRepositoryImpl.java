package cn.bit.user.dao.Impl;

import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class UserToPropertyRepositoryImpl extends AbstractMongoDao<UserToProperty, ObjectId>
        implements MongoDao<UserToProperty, ObjectId> {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
