package cn.bit.push.dao;

import cn.bit.facade.model.push.PushTemplate;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PushTemplateRepository extends MongoDao<PushTemplate, ObjectId>, MongoRepository<PushTemplate, ObjectId> {

    PushTemplate findByIdAndDataStatus(ObjectId id, Integer dataStatus);

}
