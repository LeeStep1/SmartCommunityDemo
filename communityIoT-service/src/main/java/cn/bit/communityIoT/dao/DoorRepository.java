package cn.bit.communityIoT.dao;

import cn.bit.facade.model.communityIoT.Door;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DoorRepository extends DoorRepositoryAdvice, MongoDao<Door, ObjectId>, MongoRepository<Door, ObjectId> {

    List<Door> findByCommunityIdAndBuildingIdAndDataStatus(ObjectId communityId, ObjectId buildingId, Integer dataStatus);

    List<Door> findByDeviceIdAndDataStatus(Long deviceId, Integer dataStatus);

    List<Door> findByIdInAndDataStatus(Collection<ObjectId> doorIds, Integer dataStatus);

    Long countByCommunityIdAndDataStatus(ObjectId communityId, Integer dataStatus);

    Long countByCommunityIdAndOnlineStatusAndDataStatus(ObjectId communityId, Integer onlineStatus, Integer dataStatus);

    <T> Page<T> findByCommunityIdAndBuildingIdAndDeviceCodeAndBrandNoAndNameRegexAndOnlineStatusAndDataStatusAllIgnoreNull(
            ObjectId communityId, ObjectId buildingId, String deviceCode, Integer brandNo, String name,
            Integer onlineStatus, int dataStatus, Pageable pageable, Class<T> tClass);

    @Query(value = "{'communityId' : ?0, 'mac' : ?1, 'dataStatus' : 1}")
    Door findByCommunityIdAndMac(ObjectId communityId, String mac);

	Door updateByIdAndDataStatus(Door door, ObjectId id, int dataStatus);

    Door findByIdAndDataStatus(ObjectId id, int dataStatus);

    Door findByDeviceIdAndBrandNoAndDeviceCodeAndDataStatus(Long deviceId, Integer brandNo, String deviceCode, int dataStatus);

    List<Door> findByCommunityIdAndBuildingIdInAndDoorTypeAndDataStatusOrCommunityIdAndDoorTypeAndDataStatus(ObjectId communityId1, Set<ObjectId> buildingIds, int doorType1, int dataStatus1, ObjectId communityId2, int doorType2, int dataStatus2);

    List<Door> findByCommunityIdAndBuildingIdInAndDoorTypeAndBrandNoAndDataStatusOrCommunityIdAndDoorTypeAndBrandNoAndDataStatus(ObjectId communityId1, Set<ObjectId> buildingIds, int doorType1, Integer brandNo1, int dataStatus1, ObjectId communityId2, int doorType2, Integer brandNo2, int dataStatus2);

    List<Door> findByCommunityIdAndDataStatus(ObjectId communityId, int dataStatus);

	List<Door> findByCommunityIdAndBuildingIdInAndDoorTypeAndBrandNoInAndServiceIdAndDataStatusOrCommunityIdAndDoorTypeAndBrandNoInAndServiceIdAndDataStatus(
	        ObjectId communityId, Set<ObjectId> buildingIds, int doorType, Set<Integer> brandNo, Set<Integer> serviceId, int dataStatus,
            ObjectId communityId1, int doorType1, Set<Integer> brandNo1, Set<Integer> serviceId1, int dataStatus1);

    Door findByCommunityIdAndDeviceCodeAndDataStatus(ObjectId communityId, String deviceCode, int dataStatus);

    List<Door> findByCommunityIdIgnoreNullAndTerminalCodeIgnoreNullAndBrandNoInAndDataStatus(ObjectId communityId, String terminalCode, Set<Integer> brandNos, Integer dataStatus);
}
