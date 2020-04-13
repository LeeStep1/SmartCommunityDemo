package cn.bit.task.dao;

import cn.bit.facade.model.task.Schedule;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ScheduleRepository extends MongoDao<Schedule, ObjectId>, MongoRepository<Schedule, ObjectId> {

    List<Schedule> findByCommunityIdAndPostCodeAndDataStatusAndAttendTimeLessThanEqualAndOffTimeGreaterThanEqual(
            ObjectId communityId, String postCode, int dataStatus, Date attendTime, Date offTime);

    Schedule updateById(Schedule toUpdate, ObjectId scheduleId);

    long updateByIdIn(Schedule toUpdate, Set<ObjectId> ids);

    Page<Schedule> findByCommunityIdAndPostCodeIgnoreNullAndUserIdIgnoreNullAndDataStatusAndWorkDateGreaterThanEqualAndWorkDateLessThanEqual(
            ObjectId communityId, String postCode, ObjectId userId, int dataStatus, Date startDate, Date endDate, Pageable pageable);

    List<Schedule> findByCommunityIdAndUserIdAndPostCodeAndDataStatusAndWorkDateGreaterThanEqualAndWorkDateLessThanEqualAndAttendTimeLessThanEqualAndOffTimeGreaterThanEqualAllIgnoreNull(
            ObjectId communityId, ObjectId userId, String postCode, int dataStatus,
            Date startDate, Date endDate, Date attendTime, Date offTime);

    long updateByCommunityIdAndPostCodeAndWorkDateGreaterThanEqualAndWorkDateLessThanEqual(
            Schedule toDelete, ObjectId communityId, String postCode, Date startDate, Date endDate);
}
