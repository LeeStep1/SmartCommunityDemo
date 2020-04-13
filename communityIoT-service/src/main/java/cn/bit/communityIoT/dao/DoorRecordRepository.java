package cn.bit.communityIoT.dao;

import cn.bit.facade.model.communityIoT.DoorRecord;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface DoorRecordRepository extends DoorRecordRepositoryAdvice, MongoDao<DoorRecord, ObjectId>,
        MongoRepository<DoorRecord, ObjectId> {

	DoorRecord updateById(DoorRecord entity, ObjectId id);

    Long countByCommunityIdAndTimeGreaterThanEqualAndTimeLessThanAndDataStatus(
            ObjectId communityId, Date startAt, Date endAt, int dataStatus);
}
