package cn.bit.vehicle.dao;

import cn.bit.facade.model.vehicle.Apply;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ApplyRepository extends MongoRepository<Apply, ObjectId> {
    Apply findByIdAndDataStatus(ObjectId id, Integer dataStatus);

    List<Apply> findByUserIdAndCommunityIdAndDataStatusOrderByCreateAtDesc(
            ObjectId userId, ObjectId communityId, Integer dataStatus);

    Apply findByCarNoAndDataStatus(String carNo, Integer dataStatus);

    List<Apply> findCarByUserIdAndCommunityIdAndAuditStatusAndDataStatusOrderByCreateAtDesc(
            ObjectId userId, ObjectId communityId, int auditStatus, int dataStatus);

    Apply findByCarNoAndCommunityIdAndAuditStatusAndDataStatus(
            String carNo, ObjectId communityId, int auditStatus, int dataStatus);

    List<Apply> findByCarNoAndCommunityIdAndAuditStatusAndDataStatusOrCarNoAndCommunityIdAndAuditStatusAndUserIdAndDataStatus(
            String carNo, ObjectId communityId, int auditStatus, int dataStatus,
            String carNo1, ObjectId communityId1, int auditStatus1, ObjectId userId, int dataStatus1);

    Page<Apply> findByCarNoRegexIgnoreNullAndCommunityIdAndAuditStatusIgnoreNullAndDataStatus(
            String carNo, ObjectId communityId, Integer auditStatus, int dataStatus, Pageable pageable);

	Apply updateById(Apply apply, ObjectId carId);
}
