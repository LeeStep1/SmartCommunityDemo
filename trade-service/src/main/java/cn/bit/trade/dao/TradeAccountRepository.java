package cn.bit.trade.dao;

import cn.bit.facade.model.trade.TradeAccount;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface TradeAccountRepository extends MongoDao<TradeAccount, ObjectId>, MongoRepository<TradeAccount, ObjectId> {

    TradeAccount findByIdAndDataStatus(ObjectId id, Integer dataStatus);

    TradeAccount findByPlatformAndAppIdAndDataStatus(Integer platform, String appId, Integer dataStatus);

    List<TradeAccount> findByIdInAndDataStatus(Collection<ObjectId> ids, Integer dataStatus);

}
