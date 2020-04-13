package cn.bit.business.dao;

import cn.bit.facade.model.business.BizSlide;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/2
 */
public interface BizSlideRepository extends MongoDao<BizSlide, ObjectId>, MongoRepository<BizSlide, ObjectId> {
	BizSlide updateById(BizSlide toUpdate, ObjectId id);

	BizSlide findByIdAndDataStatus(ObjectId id, int dataStatus);

	Page<BizSlide> findByShopNameRegexIgnoreNullAndTitleRegexIgnoreNullAndDataStatus(String shopName, String title, int dataStatus, Pageable pageable);

	Long countByCommunityIdAndPublishedAndDataStatus(ObjectId communityId, Integer published, int dataStatus);

	<T> List<T> findByCommunityIdAndPublishedAndDataStatusOrderByRankAsc(ObjectId communityId, Integer published, int dataStatus, Class<T> tClass);
}
