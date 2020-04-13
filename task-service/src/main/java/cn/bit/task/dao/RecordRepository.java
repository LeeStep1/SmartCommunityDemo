package cn.bit.task.dao;

import cn.bit.facade.model.task.Record;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface RecordRepository extends MongoDao<Record, ObjectId>, MongoRepository<Record, ObjectId> {

    Page<Record> findByUserNameIgnoreNullAndTaskTypeIgnoreNullAndDataStatus(String userName, Integer taskType, int dataStatus, Pageable pageable);

    List<Record> findByUserNameIgnoreNullAndTaskTypeIgnoreNullAndDataStatusOrderByCreateAtDesc(String userName, Integer taskType, int dataStatus);

    Record updateById(Record record, ObjectId id);

    Record findByIdAndDataStatus(ObjectId id, int dataStatus);

    Page<Record> findByCommunityIdAndUserIdInAndUserNameAndTaskTypeAndPostCodeAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
            ObjectId communityId, Collection<ObjectId> userIds, String userName, Integer taskType, String postCode,
            Date startDate, Date endDate, int dataStatus, Pageable pageable);

    List<Record> findByCommunityIdAndUserIdInAndUserNameAndTaskTypeAndPostCodeAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
            ObjectId communityId, Collection<ObjectId> userIds, String userName, Integer taskType, String postCode,
            Date startDate, Date endDate, int dataStatus);
}
