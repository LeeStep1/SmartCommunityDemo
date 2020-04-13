package cn.bit.community.dao;

import cn.bit.facade.model.community.District;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface DistrictRepository extends MongoDao<District, ObjectId>,
        MongoRepository<District, ObjectId> {

    List<District> findByCommunityIdAndDataStatus(ObjectId communityId, int dataStatus);

    List<District> findByIdInAndDataStatus(Collection<ObjectId> id, int dataStatus);

    District updateByIdAndDataStatus(District district, ObjectId id, Integer dataStatus);

    int countByNameAndCommunityIdAndDataStatus(String name, ObjectId communityId, Integer dataStatus);

    List<District> findByNameAndCommunityIdAndDataStatus(String name, ObjectId communityId, Integer dataStatus);

    District findByBuildingIdsAndDataStatus(Collection<ObjectId> buildingIds, Integer dataStatus);

	District updateById(District toUpdate, ObjectId id);

    Page<District> findByCommunityIdAndDataStatus(ObjectId communityId, int key, Pageable pageable);
}
