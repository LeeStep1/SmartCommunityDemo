package cn.bit.business.dao;

import cn.bit.facade.model.business.Goods;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by fxiao
 * on 2018/4/6
 */
public interface GoodsRepository extends MongoDao<Goods, ObjectId>, MongoRepository<Goods, ObjectId> {
	Page<Goods> findByNameRegexIgnoreNullAndShopIdIgnoreNullAndDataStatus(String name, ObjectId shopId, int dataStatus, Pageable pageable);

	List<Goods> findByShopIdAndDataStatusOrderByCreateAtAsc(ObjectId shopId, int dataStatus);

	Goods updateByIdAndDataStatus(Goods toUpdate, ObjectId id, int dataStatus);
}
