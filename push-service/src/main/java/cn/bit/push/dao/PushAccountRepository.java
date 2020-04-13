package cn.bit.push.dao;

import cn.bit.facade.model.push.PushAccount;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PushAccountRepository extends MongoDao<PushAccount, ObjectId>, MongoRepository<PushAccount, ObjectId> {

    PushAccount findByIdAndDataStatus(ObjectId id, Integer dataStatus);

}
