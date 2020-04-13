package cn.bit.fees.dao;

import cn.bit.facade.model.fees.PropBillDetail;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface PropBillDetailRepository extends MongoDao<PropBillDetail, ObjectId>, MongoRepository<PropBillDetail, ObjectId>, PropBillDetailRepositoryAdvice
{
    List<PropBillDetail> findByBillIdAndTypeAndDataStatus(ObjectId billId,Integer type, Integer dataStatus);

    List<PropBillDetail> findByBillIdAndDataStatus(ObjectId billId, Integer key);

    PropBillDetail updateById(PropBillDetail toUpdate, ObjectId id);

	List<PropBillDetail> findByBillIdInAndDataStatus(Set<ObjectId> collect, int dataStatus);
}
