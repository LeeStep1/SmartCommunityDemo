package cn.bit.business.dao;

import cn.bit.facade.model.business.Convenience;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/4
 */
public interface ConvenienceRepository extends MongoDao<Convenience, ObjectId>, MongoRepository<Convenience, ObjectId> {

	Convenience updateByIdAndDataStatus(Convenience toUpdate, ObjectId id, int dataStatus);

	Page<Convenience> findByCommunityIdIgnoreNullAndNameRegexIgnoreNullAndDataStatus(ObjectId communityId, String name, int dataStatus, Pageable pageable);

	Convenience findByIdAndDataStatus(ObjectId id, int dataStatus);

	<T> List<T> findByCommunityIdAndDataStatusOrderByRankAsc(ObjectId communityId, int dataStatus, Class<T> tClass);
}
