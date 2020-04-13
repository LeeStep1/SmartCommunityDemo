package cn.bit.property.dao;

import cn.bit.facade.model.property.Gtaskzs;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by fxiao
 * on 2018/3/26
 */
public interface GtaskzsRepository extends MongoDao<Gtaskzs, ObjectId>, MongoRepository<Gtaskzs, ObjectId> {

	Gtaskzs updateByOtherIdAndDataStatus(Gtaskzs toUpdate, ObjectId otherId, int dataStatus);
}
