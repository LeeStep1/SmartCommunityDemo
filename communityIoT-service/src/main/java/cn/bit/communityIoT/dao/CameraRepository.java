package cn.bit.communityIoT.dao;

import cn.bit.facade.model.communityIoT.Camera;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface CameraRepository extends MongoDao<Camera, ObjectId>, MongoRepository<Camera, ObjectId> {

	Camera updateByIdAndDataStatus(Camera camera, ObjectId id, int dataStatus);

	Integer updateByBrandNoAndDataStatus(Camera camera, Integer brandNo, int dataStatus);

	Camera findByBrandNoAndDataStatus(Integer brandNo, int dataStatus);

	Page<Camera> findByCommunityIdAndBuildingIdInAndCameraCodeRegexAndBrandNoAndNameRegexAndCameraStatusAndUpdateAtGreaterThanAndDataStatusAllIgnoreNull(
			ObjectId communityId, Set<ObjectId> buildingIdSet, String cameraCode, Integer brandNo, String name,
			Integer cameraStatus, Date after, int dataStatus, Pageable pageable);

	List<Camera> findByCommunityIdAndBuildingIdInAndCameraCodeRegexAndBrandNoAndNameRegexAndCameraStatusAndUpdateAtGreaterThanAndDataStatusAllIgnoreNullOrderByCreateAtAsc(
			ObjectId communityId, Set<ObjectId> buildingIdSet, String cameraCode, Integer brandNo, String name,
			Integer cameraStatus, Date after, int dataStatus);

    Camera findByIdAndDataStatus(ObjectId id, int dataStatus);
}
