package cn.bit.community.service;

import cn.bit.community.dao.CommunityTradeAccountRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.community.CommunityTradeAccount;
import cn.bit.facade.service.community.CommunityTradeAccountFacade;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("communityTradeAccountFacade")
public class CommunityTradeAccountFacadeImpl implements CommunityTradeAccountFacade {
    @Autowired
    private CommunityTradeAccountRepository repository;

    @Override
    public CommunityTradeAccount addCommunityTradeAccount(CommunityTradeAccount communityTradeAccount) {
        communityTradeAccount.setCreateAt(new Date());
        communityTradeAccount.setUpdateAt(communityTradeAccount.getCreateAt());
        communityTradeAccount.setDataStatus(DataStatusType.VALID.KEY);
        return repository.insert(communityTradeAccount);
    }

    @Override
    public CommunityTradeAccount getCommunityTradeAccountById(ObjectId id) {
        return repository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public CommunityTradeAccount updateCommunityTradeAccount(CommunityTradeAccount communityTradeAccount) {
        communityTradeAccount.setUpdateAt(new Date());
        return repository.updateOne(communityTradeAccount);
    }

    @Override
    public void deleteCommunityTradeAccount(ObjectId id) {
        CommunityTradeAccount communityTradeAccount = new CommunityTradeAccount();
        communityTradeAccount.setId(id);
        communityTradeAccount.setUpdateAt(new Date());
        communityTradeAccount.setDataStatus(DataStatusType.VALID.KEY);
        repository.updateOne(communityTradeAccount);
    }

    @Override
    public List<CommunityTradeAccount> getCommunityTradeAccountsByCommunityId(ObjectId communityId) {
        return repository.findByCommunityIdAndDataStatus(communityId, DataStatusType.VALID.KEY);
    }

    @Override
    public List<CommunityTradeAccount> getCommunityTradeAccountsByCommunityIdAndClient(ObjectId communityId, Integer client) {
        return repository.findByCommunityIdAndClientAndDataStatus(communityId, client, DataStatusType.VALID.KEY);
    }

    @Override
    public CommunityTradeAccount getCommunityTradeAccountByCommunityIdAndClientAndPlatfrom(ObjectId communityId, Integer client,
                                                                                           Integer platform) {
        return repository.findByCommunityIdAndClientAndPlatformAndDataStatus(communityId, client, platform,
                DataStatusType.VALID.KEY);
    }
}
