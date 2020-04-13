package cn.bit.facade.service.community;

import cn.bit.facade.model.community.CommunityTradeAccount;
import org.bson.types.ObjectId;

import java.util.List;

public interface CommunityTradeAccountFacade {

    CommunityTradeAccount addCommunityTradeAccount(CommunityTradeAccount communityTradeAccount);

    CommunityTradeAccount getCommunityTradeAccountById(ObjectId id);

    CommunityTradeAccount updateCommunityTradeAccount(CommunityTradeAccount communityTradeAccount);

    void deleteCommunityTradeAccount(ObjectId id);

    List<CommunityTradeAccount> getCommunityTradeAccountsByCommunityId(ObjectId communityId);

    List<CommunityTradeAccount> getCommunityTradeAccountsByCommunityIdAndClient(ObjectId communityId, Integer client);

    CommunityTradeAccount getCommunityTradeAccountByCommunityIdAndClientAndPlatfrom(ObjectId communityId, Integer client,
                                                                                    Integer platform);

}
