package cn.bit.user.dao;

import cn.bit.facade.model.user.Household;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface HouseholdRepository
        extends MongoDao<Household, ObjectId>, MongoRepository<Household, ObjectId> {

    List<Household> findByRoomIdAndDataStatusOrderByRelationshipAsc(ObjectId roomId, int dataStatus);

    Page<Household> findByCommunityIdAndBuildingIdAndRoomIdAndRoomNameRegexAndUserNameRegexAndPhoneAndRelationshipAndActivatedAndDataStatusAllIgnoreNull(
            ObjectId communityId, ObjectId buildingId, ObjectId roomId, String roomName, String userName, String phone,
            Integer relationship, Boolean activated, Integer dataStatus, Pageable pageable);

    Household updateById(Household household, ObjectId id);

    List<Household> findByRoomIdAndRelationshipAndDataStatus(ObjectId roomId, Integer relationship, int dataStatus);

    List<Household> findByPhoneAndRelationshipAndActivatedNotAndDataStatus(
            String phone, Integer relationship, boolean activated, int dataStatus);

    Long updateByIdIn(Household toUpdate, Collection<ObjectId> householdIds);

    Long updateByRoomIdAndDataStatus(Household toRemove, ObjectId roomId, int dataStatus);

    Household updateByRoomIdAndUserIdAndDataStatus(Household toRemove, ObjectId roomId, ObjectId userId, int dataStatus);

    Household upsertByRoomIdAndUserNameAndRelationship(Household household, ObjectId roomId, String userName, Integer relationship);

    List<Household> findByRoomIdAndUserIdAndDataStatusAllIgnoreNull(ObjectId roomId, ObjectId userId, Integer dataStatus);

    Household findByRoomIdAndUserNameIgnoreCaseAndDataStatus(ObjectId roomId, String userName, int dataStatus);

    List<Household> findByRoomIdInAndRelationshipAndDataStatus(Collection<ObjectId> roomIds, Integer relationship, int dataStatus);

    Household findByIdAndDataStatus(ObjectId id, int dataStatus);

    List<Household> findByCommunityIdAndUserNameRegexIgnoreNullAndActivatedAndDataStatus(
            ObjectId communityId, String userName, Boolean activated, int dataStatus);

    List<Household> findByCommunityIdAndRelationshipAndDataStatus(ObjectId communityId, Integer relationship, int dataStatus);

    List<Household> findByCommunityIdAndUserIdAndActivatedAndDataStatus(ObjectId communityId, ObjectId userId, Boolean activated, int dataStatus);

    List<Household> findByRoomIdInAndUserIdAndDataStatus(Collection<ObjectId> roomIds, ObjectId userId, int dataStatus);
}
