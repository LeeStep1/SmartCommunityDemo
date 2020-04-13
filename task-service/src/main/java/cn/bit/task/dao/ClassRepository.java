package cn.bit.task.dao;

import cn.bit.facade.model.task.Class;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClassRepository extends MongoDao<Class, ObjectId>, MongoRepository<Class, ObjectId> {
    List<Class> findByCommunityIdAndPostCodeAndDataStatus(ObjectId communityId, String postCode, Integer dataStatus);
    Class updateWithUnsetIfNullAttendPlaceAndAttendTimeAndTaskAndOffPlaceAndOffTimeAndRestWeeksByIdAndDataStatus(Class entity, ObjectId id, Integer dataStatus);

    List<Class> queryListByCommunityIdAndPostCodeAndTypeAndDataStatus(ObjectId communityId, String postCode, Integer classType, int dataStatus);

    Class updateById(Class toUpdate, ObjectId id);

    Boolean existsByCommunityIdAndNameAndPostCodeAndDataStatus(ObjectId communityId, String name, String postCode, int dataStatus);

    Boolean existsByCommunityIdAndTypeAndPostCodeAndDataStatus(ObjectId communityId, Integer type, String postCode, int dataStatus);
}
