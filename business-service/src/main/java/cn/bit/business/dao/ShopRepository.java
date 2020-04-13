package cn.bit.business.dao;

import cn.bit.facade.model.business.Shop;
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
public interface ShopRepository extends MongoDao<Shop, ObjectId>, MongoRepository<Shop, ObjectId> {

	Shop updateWithUnsetIfNullLogoByIdAndDataStatus(Shop toUpdate, ObjectId shopId, int dataStatus);

	Shop findByIdAndDataStatus(ObjectId id, int dataStatus);

	Page<Shop> findByNameRegexAndProvinceAndCityAndDistrictAndCommunityIdsInAndDataStatusAllIgnoreNull(String name, String province, String city, String district, ObjectId communityId, int dataStatus, Pageable pageable);

	List<Shop> findByCommunityIdsInAndTypeAndTagInAndDataStatusAllIgnoreNullOrderByCreateAtAsc(ObjectId communityId, Integer type, String tag, int dataStatus);

	Shop updateWithIncPopularityById(Shop toUpdate, ObjectId id);

	Shop updateById(Shop shop, ObjectId objectId);

	<T> Page<T> findByCommunityIdsAndTypeAndTagAndDataStatusAllIgnoreNull(ObjectId communityId, Integer type, String tag, int dataStatus, Pageable pageable, Class<T> tClass);

	<T> T findTop1ByCommunityIdsAndDataStatusOrderByPopularityDesc(ObjectId communityId, int dataStatus, Class<T> tClass);

	Shop updateWithUnsetIfNullCouponNamesByIdAndDataStatus(Shop shop, ObjectId id, int dataStatus);
}
