package cn.bit.system.dao;

import cn.bit.facade.model.system.ThirdApp;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ThirdAppRepository extends MongoDao<ThirdApp, ObjectId>, MongoRepository<ThirdApp, ObjectId> {
    ThirdApp updateById(ThirdApp thirdApp, ObjectId id);

    ThirdApp findByIdAndDataStatus(ObjectId id, int dataStatus);

}
