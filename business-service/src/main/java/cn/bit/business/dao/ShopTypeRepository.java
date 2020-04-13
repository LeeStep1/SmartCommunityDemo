package cn.bit.business.dao;

import cn.bit.facade.model.business.ShopType;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by fxiao
 * on 2018/4/2
 */
public interface ShopTypeRepository extends MongoDao<ShopType, ObjectId>, MongoRepository<ShopType, ObjectId> {
    ShopType getFirstByCodeAndDataStatus(Integer code,Integer dataStatus);
}
