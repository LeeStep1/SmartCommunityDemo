package cn.bit.business.dao;

import cn.bit.facade.model.business.Coupon;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by fxiao
 * on 2018/4/3
 */
public interface CouponRepository extends MongoDao<Coupon, ObjectId>, MongoRepository<Coupon, ObjectId> {
	int updateByShopIdAndDataStatus(Coupon toUpdate, ObjectId shopId, int dataStatus);

	List<Coupon> findByShopIdAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatusOrderByCreateAtAsc(ObjectId shopId, int validStatus, Date validityEndAt, int dataStatus);

	Coupon updateWithIncUseNumByIdAndDataStatus(Coupon toUpdate, ObjectId id, int dataStatus);

	Coupon updateWithIncReceiveNumByIdAndValidStatusAndReceiveNumLessThanAndDataStatus(Coupon toUpdate, ObjectId id, int validStatus, Integer receiveNum, int dataStatus);

	/**
	 * 获取过期的优惠券
	 * @param validStatus
	 * @param dataStatus
	 * @param validityEndAt
	 * @return
	 */
	List<Coupon> findByValidStatusAndDataStatusAndValidityEndAtLessThan(int validStatus, int dataStatus, Date validityEndAt);

	Coupon updateById(Coupon coupon, ObjectId id);

	long updateByIdIn(Coupon coupon, Set<ObjectId> ids);

	List<Coupon> findTop2ByShopIdAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatusOrderByCreateAtAsc(ObjectId shopId, int validStatus, Date validityEndAt, int dataStatus);

	<T> T findTop1ByCommunityIdsAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatusOrderByMaxPriceDesc(ObjectId communityId, int validStatus, Date validityEndAt, int dataStatus, Class<T> tClass);

	<T> Page<T> findByCommunityIdsAndShopTypeIgnoreNullAndValidStatusAndValidityEndAtGreaterThanEqualAndDataStatus(ObjectId communityId, Integer shopType, int validStatus, Date validityEndAt, int dataStatus, Pageable pageable, Class<T> tClass);

	List<Coupon> findByShopIdInAndValidStatusAndDataStatus(Collection<ObjectId> shopIds, int validStatus, int dataStatus);

	List<Coupon> findByValidStatusAndDataStatusAndShopIdInAndValidityEndAtGreaterThanEqualOrderByCreateAtDesc(int validStatus, int dataStatus, Collection<ObjectId> shopIds, Date endAt);

	long updateWithValidStatusAndDataStatusByShopId(Coupon coupon, ObjectId shopId);
}
