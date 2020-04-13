package cn.bit.user.dao;

import cn.bit.facade.model.user.CommunityUser;
import org.bson.types.ObjectId;

import java.util.Collection;

public interface CommunityUserRepositoryAdvice {

    int pullAllByCommunityIdAndUserId(CommunityUser communityUser, ObjectId communityId, ObjectId userId);

    int pullAllByCommunityIdAndUserId(CommunityUser communityUser, ObjectId communityId, Collection<ObjectId> userIds);
}
