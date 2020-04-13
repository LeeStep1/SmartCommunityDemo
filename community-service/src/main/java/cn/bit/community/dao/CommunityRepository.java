package cn.bit.community.dao;

import cn.bit.facade.model.community.Community;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface CommunityRepository extends MongoDao<Community, ObjectId>, MongoRepository<Community, ObjectId> {

    List<Community> findByIdIn(Collection<ObjectId> ids);

    int upsertWithIncHouseholdCntAndCheckInRoomCntById(Community community, ObjectId id);

    Community updateById(Community toUpdate, ObjectId id);

    Community findByCode(String code);

    <T> List<T> findByDataStatusAndOpenIgnoreNull(int dataStatus, Boolean open, Class<T> tClass);

    List<Community> findByMiliCIdIsNotNullAndDataStatus(Integer dataStatus);

    Community upsertWithAddToSetDeviceBrandsThenSetOnInsertDataStatusAndCreateAtById(Community community, ObjectId id);

    Community updateWithPullAllDeviceBrandsById(Community community, ObjectId id);
}
