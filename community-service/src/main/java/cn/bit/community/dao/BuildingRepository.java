package cn.bit.community.dao;

import cn.bit.facade.model.community.Building;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BuildingRepository extends MongoDao<Building, ObjectId>, MongoRepository<Building, ObjectId> {

    @Query(value = "{'communityId': ?0, 'dataStatus': 1}", fields = "{'_id': 1}")
    List<Building> findByCommunityId(ObjectId communityId);

    Building findByOutIdAndDataStatus(String outId, Integer dataStatus);

	Building updateById(Building toUpdate, ObjectId id);

	Building findByIdAndDataStatus(ObjectId id, int dataStatus);

    List<Building> findAllByCommunityIdInAndMiliBIdIsNotNull(List<ObjectId> communityIds);

    List<Building> findByIdInAndDataStatus(Collection<ObjectId> collect, Integer dataStatus);

    void updateByIdIn(Building building, List<ObjectId> ids);
}
