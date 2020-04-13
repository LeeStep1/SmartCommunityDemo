package cn.bit.push.dao;

import cn.bit.facade.model.push.PushConfig;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface PushConfigRepository extends MongoDao<PushConfig, ObjectId>, MongoRepository<PushConfig, ObjectId> {

    PushConfig findByIdAndDataStatus(ObjectId id, Integer dataStatus);

    Page<PushConfig> findByCompanyIdAndPointNameRegexIgnoreNullAndDataStatus(
            ObjectId companyId, String name, Integer dataStatus, Pageable pageable);

    PushConfig updateByIdAndDataStatus(PushConfig toUpdate, ObjectId id, Integer dataStatus);

    PushConfig findByCompanyIdAndPointIdAndDataStatus(ObjectId companyId, String pointId, Integer dataStatus);

    List<PushConfig> findByTargetsAndDataStatus(String roleId, Integer dataStatus);

    void updateWithAddToSetTargetsByCompanyIdAndPointIdInAndDataStatus(PushConfig toUpdate, ObjectId companyId,
                                                                       Collection<String> pointIds, Integer dataStatus);

    void updateWithPullAllTargetsByCompanyIdAndPointIdInAndDataStatus(PushConfig toUpdate, ObjectId companyId,
                                                                      Collection<String> pointIds, Integer dataStatus);
}
