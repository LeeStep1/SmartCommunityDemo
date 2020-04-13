package cn.bit.property.dao;

import cn.bit.facade.model.property.ReleasePass;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReleasePassRepository extends MongoDao<ReleasePass, ObjectId>, MongoRepository<ReleasePass, ObjectId>, ReleasePassRepositoryAdvice {

    List<ReleasePass> findAllByCommunityIdAndDataStatusOrderByUpdateAtDesc(ObjectId communityId, Integer dataStatus);

	ReleasePass updateById(ReleasePass toUpdate, ObjectId id);

	ReleasePass findByIdAndCommunityIdAndDataStatus(ObjectId id, ObjectId communityId, int dataStatus);
}
