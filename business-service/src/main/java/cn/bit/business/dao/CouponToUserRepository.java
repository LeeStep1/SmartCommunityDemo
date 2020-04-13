package cn.bit.business.dao;

import cn.bit.facade.model.business.CouponToUser;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/3
 */
public interface CouponToUserRepository extends MongoDao<CouponToUser, ObjectId>,
		MongoRepository<CouponToUser, ObjectId> {
	Boolean existsByCouponIdAndUserId(ObjectId id, ObjectId userId);

	CouponToUser updateByIdAndDataStatus(CouponToUser toUpdate, ObjectId id, int dataStatus);

	long countByCouponIdAndDataStatus(ObjectId couponId, int dataStatus);

	long countByCouponIdAndUserId(ObjectId couponId, ObjectId userId);

	CouponToUser findByCouponIdAndUserId(ObjectId couponId, ObjectId userId);

	//Page<CouponToUser> findByNameLikeIgnoreNullAndShopIdAndDataStatus(String name, ObjectId shopId, Integer dataStatus, Pageable pageable);
	Page<CouponToUser> findByUserIdAndShopIdIgnoreNullAndDataStatus(ObjectId userId, ObjectId shopId, Integer dataStatus, Pageable pageable);

	Page<CouponToUser> findByUserIdAndUseStatusNotAndValidityEndAtGreaterThanEqualAndDataStatus(ObjectId userId, Integer useStaus, Date validityEndAt, Integer dataStatus, Pageable pageable);

	Page<CouponToUser> findByUserIdAndUseStatusAndDataStatus(ObjectId userId, Integer useStatus, Integer dataStatus, Pageable pageable);

	Page<CouponToUser> findByUserIdAndUseStatusNotAndValidityEndAtLessThanAndDataStatus(ObjectId userId, Integer useStatus, Date validityEndAt, int dataStatus, Pageable pageable);

	List<CouponToUser> findByUserIdAndValidityEndAtGreaterThanEqual(ObjectId userId, Date validityEndAt);
}
