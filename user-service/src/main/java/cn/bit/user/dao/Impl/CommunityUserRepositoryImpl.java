package cn.bit.user.dao.Impl;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.user.CommunityUser;
import cn.bit.framework.data.mongodb.MongoDao;
import cn.bit.framework.data.mongodb.impl.AbstractMongoDao;
import cn.bit.user.dao.CommunityUserRepositoryAdvice;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collection;

public class CommunityUserRepositoryImpl extends AbstractMongoDao<CommunityUser, ObjectId> implements CommunityUserRepositoryAdvice, MongoDao<CommunityUser, ObjectId> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public int pullAllByCommunityIdAndUserId(CommunityUser communityUser, ObjectId communityId, ObjectId userId) {
        return pullAllBy(communityUser, communityId, userId, DataStatusType.VALID.KEY);
    }

    @Override
    public int pullAllByCommunityIdAndUserId(CommunityUser communityUser, ObjectId communityId, Collection<ObjectId> userIds) {
        return pullAllBy(communityUser, communityId, userIds, DataStatusType.VALID.KEY);
    }
}
