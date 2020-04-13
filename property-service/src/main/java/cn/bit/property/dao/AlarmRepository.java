package cn.bit.property.dao;

import cn.bit.facade.model.property.Alarm;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AlarmRepository extends MongoDao<Alarm, ObjectId>, MongoRepository<Alarm, ObjectId> {
    Alarm findByIdAndDataStatus(ObjectId id, Integer dataStatus);

    List<Alarm> findAllByDataStatusOrderByCallTimeDescReceiveStatusAsc(Integer dataStatus);

    Long countByCommunityIdAndReceiveStatusAndDataStatus(ObjectId communityId, Integer receiveStatus, Integer dataStatus);

	Page<Alarm> findByCommunityIdAndBuildingIdIgnoreNullAndReceiveStatusIgnoreNullAndCallerNameRegexIgnoreNullAndDataStatus(ObjectId communityId
			, ObjectId buildingId, Integer receiveStatus, String callerName, int dataStatus, Pageable pageable);

	Page<Alarm> findByCallerIdAndDataStatus(ObjectId callerId, int dataStatus, Pageable pageable);

	Alarm updateById(Alarm alarm, ObjectId id);
}
