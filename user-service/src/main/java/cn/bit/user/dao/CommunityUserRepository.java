package cn.bit.user.dao;

import cn.bit.facade.model.user.CommunityUser;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface CommunityUserRepository extends CommunityUserRepositoryAdvice, MongoDao<CommunityUser, ObjectId>,
		MongoRepository<CommunityUser, ObjectId> {

    CommunityUser findByCommunityIdAndUserIdAndDataStatus(ObjectId communityId, ObjectId userId, Integer dataStatus);

    CommunityUser findByCommunityIdAndUserIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
    		ObjectId communityId, ObjectId uid, Collection<Integer> clients, Collection<String> roles, Integer dataStatus);

    Page<CommunityUser> findByCommunityIdAndUserIdInAndRolesInAndFaceStatusInAndDataStatusAllIgnoreNullOrderByUpdateAtDesc(
    		ObjectId communityId, Collection<ObjectId> userIds, Collection<String> roles,
		    Collection<Integer> faceStatus, int dataStatus, Pageable pageable);

    CommunityUser updateByUserIdAndCommunityIdAndDataStatus(CommunityUser communityUser,
                                                            ObjectId userId, ObjectId communityId, Integer dataStatus);

    List<CommunityUser> findByUserIdAndDataStatus(ObjectId userId, int dataStatus);

    CommunityUser findByFaceCodeAndDataStatus(String faceCode, Integer dataStatus);

    List<CommunityUser> updateByUserIdAndFaceCodeAndDataStatus(CommunityUser communityUser,
                                                               ObjectId userId, String faceCode, Integer dataStatus);

	CommunityUser upsertWithAddToSetClientsAndRolesByCommunityIdAndUserIdAndDataStatus(CommunityUser communityUser,
																					   ObjectId communityId, ObjectId userId, int dataStatus);

	CommunityUser updateWithUnsetIfNullBuildingIdsAndDistrictIdsAndMiliUIdsThenPullAllClientsAndRolesByCommunityIdAndUserIdAndDataStatus(
			CommunityUser communityUser, ObjectId communityId, ObjectId userId, int dataStatus);

	Page<CommunityUser> findByCommunityIdAndUserIdInIgnoreNullAndClientsAndRolesInIgnoreNullAndDataStatus(
			ObjectId communityId, Collection<ObjectId> userIds, int value, Collection<String> roles, int dataStatus, Pageable pageable);

	List<CommunityUser> findByCommunityIdAndClientsInAndRolesInIgnoreNullAndDataStatus(
			ObjectId communityId, Collection<Integer> clients, Collection<String> roles, int dataStatus);

	List<CommunityUser> findByUserIdAndClientsInAndDataStatus(
			ObjectId userId, Collection<Integer> clients, int dataStatus);

    List<CommunityUser> findByCommunityIdAndUserIdInAndClientsAndRolesIgnoreNullAndDataStatus(
    		ObjectId communityId, Collection<ObjectId> userIds, int client, String role, int dataStatus);
}
