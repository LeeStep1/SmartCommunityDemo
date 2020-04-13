package cn.bit.community.dao;

import cn.bit.facade.model.community.DataLayout;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DataLayoutRepository extends MongoDao<DataLayout, ObjectId>, MongoRepository<DataLayout, ObjectId> {

    List<DataLayout> findByCommunityIdAndScreenRatioTypeAndDisplayableAllIgnoreNull(
            ObjectId communityId, Integer screenRatioType, Boolean displayable);

    DataLayout updateById(DataLayout toUpdate, ObjectId id);
}
