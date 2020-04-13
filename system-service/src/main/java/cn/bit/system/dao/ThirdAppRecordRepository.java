package cn.bit.system.dao;

import cn.bit.facade.model.system.ThirdAppRecord;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ThirdAppRecordRepository extends MongoDao<ThirdAppRecord, ObjectId>, MongoRepository<ThirdAppRecord, ObjectId> {

}
