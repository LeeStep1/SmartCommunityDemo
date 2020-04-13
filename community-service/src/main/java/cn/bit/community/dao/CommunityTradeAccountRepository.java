package cn.bit.community.dao;

import cn.bit.facade.model.community.CommunityTradeAccount;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommunityTradeAccountRepository extends MongoDao<CommunityTradeAccount, ObjectId>, MongoRepository<CommunityTradeAccount, ObjectId> {

    CommunityTradeAccount findByIdAndDataStatus(ObjectId id, Integer dataStatus);

    CommunityTradeAccount findByCommunityIdAndClientAndPlatformAndDataStatus(ObjectId communityId, Integer client,
                                                                             Integer platform, Integer dataStatus);

    List<CommunityTradeAccount> findByCommunityIdAndDataStatus(ObjectId communityId, Integer dataStatus);

    List<CommunityTradeAccount> findByCommunityIdAndClientAndDataStatus(ObjectId communityId, Integer client, Integer dataStatus);

}
