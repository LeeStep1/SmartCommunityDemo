package cn.bit.communityIoT.dao;

import cn.bit.facade.model.communityIoT.ElevatorRecord;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ElevatorRecordRepository extends ElevatorRecordRepositoryAdvice,MongoDao<ElevatorRecord, ObjectId>, MongoRepository<ElevatorRecord, ObjectId> {

	ElevatorRecord updateById(ElevatorRecord entity, ObjectId id);

	boolean existsByUniqueCodeAndDataStatus(String Unique, Integer dataStatus);
}
