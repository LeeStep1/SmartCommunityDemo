package cn.bit.push.dao;

import cn.bit.facade.model.push.PushPoint;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PushPointRepository extends MongoDao<PushPoint, ObjectId>, MongoRepository<PushPoint, ObjectId> {

    List<PushPoint> findBySignatureAndEnableIsTrueAndDataStatus(String signature, Integer dataStatus);

    Page<PushPoint> findByScopesInAndDataStatus(Object[] scopes, Integer dataStatus, Pageable pageable);

    PushPoint findByIdAndDataStatus(ObjectId id, Integer dataStatus);

}
