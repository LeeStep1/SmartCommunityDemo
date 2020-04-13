package cn.bit.fees.dao;

import cn.bit.facade.model.fees.BillDetail;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface BillDetailRepository extends MongoDao<BillDetail, ObjectId>, MongoRepository<BillDetail, ObjectId> {

    List<BillDetail> findByRelateIdInOrderByCreateAtAsc(Collection<ObjectId> relateIds);

    void deleteByRelateId(ObjectId relateId);
}
