package cn.bit.user.dao;

import cn.bit.facade.model.user.UserToRoom;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface UserToRoomRepository extends UserToRoomRepositoryAdvice,
		MongoDao<UserToRoom, ObjectId>, MongoRepository<UserToRoom, ObjectId> {

    List<UserToRoom> findByCommunityIdAndUserIdInAndAuditStatusAndDataStatus(
    		ObjectId communityId, Collection<ObjectId> userIds, Integer auditStatuses, Integer dataStatus);

    List<UserToRoom> findByBuildingIdAndUserIdAndAuditStatusAndDataStatus(
    		ObjectId buildingId, ObjectId userId, Integer auditStatuses, Integer dataStatus);

    List<UserToRoom> findByUserIdAndRoomIdInAndDataStatusAndAuditStatus(
    		ObjectId userId, Collection<ObjectId> roomId, int dataStatus, Integer auditStatus);

    Page<UserToRoom> findByCommunityIdInAndRelationshipAndAuditStatusAndDataStatus(
    		Collection<ObjectId> communityIds, Integer relationship, int auditStatus, int dataStatus, Pageable pageable);

    List<UserToRoom> findByRoomIdInAndRelationshipAndAuditStatusAndDataStatus(
    		Collection<ObjectId> roomIds, Integer relationship, Integer auditStatus, Integer dataStatus);

    int updateByCommunityIdAndRoomIdAndAuditStatusInAndDataStatus(
    		UserToRoom type, ObjectId roomId, ObjectId communityId, Collection<Integer> auditStatus, int dataStatus);

    List<UserToRoom> findByCommunityIdAndAuditStatusAndDataStatusAndCreateAtGreaterThanEqualAndCreateAtLessThan(
    		ObjectId communityId, int auditStatus, int dataStatus, Date beginDate, Date endDate);

    List<UserToRoom> findByCommunityIdAndAuditStatusAndDataStatus(ObjectId communityId, int auditStatus, int dataStatus);

    UserToRoom findByUserIdAndRoomIdAndAuditStatusInAndDataStatus(
    		ObjectId userId, ObjectId roomId, Collection<Integer> auditStatus, int dataStatus);

    UserToRoom findByRoomIdAndRelationshipAndAuditStatusAndDataStatus(
    		ObjectId roomId, Integer relationship, int auditStatus, int dataStatus);

    List<UserToRoom> findByRoomIdAndAuditStatusAndDataStatusOrderByRelationshipAsc(
    		ObjectId roomId, Integer auditStatus, Integer dataStatus);

    Page<UserToRoom> findPageByRoomIdAndDataStatusAndRelationshipInAndAuditStatus(
    		ObjectId roomId, int dataStatus, Collection<Integer> relationships, int auditStatus, Pageable pageable);

    long countByCommunityIdAndDataStatusAndAuditStatus(ObjectId communityId, int dataStatus, int auditStatus);

    List<UserToRoom> findByBuildingIdAndAuditStatusAndDataStatusOrderByRoomIdAscRelationshipAsc(
    		ObjectId buildingId, int auditStatus, int dataStatus);

    List<UserToRoom> findByBuildingIdInAndAuditStatusAndDataStatus(Collection<ObjectId> buildingIds, int auditStatus,
                                                                   int dataStatus);

    Page<UserToRoom> findByCommunityIdAndDataStatusAndRelationshipIgnoreNullAndBuildingIdIgnoreNullAndAuditStatusIgnoreNullAndContractPhoneIgnoreNullAndNameIgnoreNull(
    		ObjectId communityId, int dataStatus, Integer relationship, ObjectId buildingId, Integer auditStatus,
		    String contractPhone, String name, Pageable pageable);

    List<UserToRoom> findByUserIdAndAuditStatusAndDataStatus(ObjectId userId, int auditStatus, int dataStatus);

    Page<UserToRoom> findByRoomIdAndAuditStatusIgnoreNullAndProprietorIdIgnoreNullAndClosedIgnoreNullAndDataStatus(
    		ObjectId roomId, Integer auditStatus, ObjectId proprietorId, Boolean closed, int dataStatus, Pageable pageable);

    UserToRoom findByRoomIdAndRelationshipAndProprietorIdAndDataStatus(
    		ObjectId roomId, Integer relationship, ObjectId currUserId, int dataStatus);

    Page<UserToRoom> findByBuildingIdAndRelationshipAndAuditStatusAndClosedNotAndDataStatusAllIgnoreNull(
    		ObjectId buildingId, Integer relationship, Integer auditStatus, Boolean notClosed, int dataStatus, Pageable pageable);

    Page<UserToRoom> findByCommunityIdAndDataStatusAndAuditStatus(
    		ObjectId communityId, int dataStatus, Integer auditStatus, Pageable pageable);

	UserToRoom findTop1ByCommunityIdAndUserIdAndAuditStatusAndDataStatusOrderByAuditTimeAsc(
			ObjectId communityId, ObjectId userId, int auditStatus, int dataStatus);

	UserToRoom updateById(UserToRoom toUpdate, ObjectId id);

	List<UserToRoom> findByCommunityIdAndUserIdAndAuditStatusAndDataStatus(
			ObjectId communityId, ObjectId userId, Integer auditStatuses, Integer dataStatus);

	Boolean existsByCommunityIdAndUserIdAndAuditStatusAndInCommonUseAndDataStatus(
			ObjectId communityId, ObjectId userId, int auditStatus, Boolean inCommonUse, int dataStatus);

	UserToRoom findByCommunityIdAndUserIdAndAuditStatusAndInCommonUseAndDataStatus(
			ObjectId communityId, ObjectId userId, int auditStatus, Boolean inCommonUse, int dataStatus);

    UserToRoom findByCommunityIdAndRoomIdAndUserIdAndAuditStatusAndDataStatus(
    		ObjectId communityId, ObjectId roomId, ObjectId userId, int auditStatus, int dataStatus);

	UserToRoom findByIdAndCommunityIdAndUserIdAndAuditStatusAndDataStatus(
			ObjectId toRoomId, ObjectId communityId, ObjectId userToRoomId, int auditStatus, int dataStatus);

    List<UserToRoom> findByRoomIdAndRelationshipAndAuditStatusInAndDataStatus(
    		ObjectId roomId, Integer relationship, Collection<Integer> auditStatus, int dataStatus);

	UserToRoom findByRoomIdAndUserIdAndRelationshipAndAuditStatusAndDataStatus(
			ObjectId roomId, ObjectId userId, Integer relationship, int auditStatus, int dataStatus);
}
