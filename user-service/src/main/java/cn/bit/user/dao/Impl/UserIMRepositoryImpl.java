package cn.bit.user.dao.Impl;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.user.IMUser;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.user.dao.UserIMRepositoryAdvice;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;


public class UserIMRepositoryImpl  extends AbstractMongoDao<IMUser, ObjectId>  implements UserIMRepositoryAdvice, MongoDao<IMUser, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public int pullAllByUserIdAndRole(IMUser imUser, ObjectId userId, String role) {
        return pullAllBy(imUser, userId, role, DataStatusType.VALID.KEY);
    }

    @Override
    public IMUser updateOneByUserIdAndRole(IMUser imUser) {
        return updateOneBy(imUser, true, imUser.getUserId(), imUser.getRole(), DataStatusType.VALID.KEY);
    }
}