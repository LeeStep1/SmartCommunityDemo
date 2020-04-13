package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Silent;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SilentRepository
        extends SilentRepositoryAdvice, MongoDao<Silent, ObjectId>, MongoRepository<Silent, ObjectId> {
}
