package cn.bit.vehicle.dao;

import cn.bit.facade.model.vehicle.Identity;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface IdentityRepository extends MongoRepository<Identity, ObjectId> {


    Page<Identity> findByCommunityIdAndCarTypeAndTypeAndEndAtAfterAndCarNoAndDataStatusOrCommunityIdAndCarTypeAndTypeAndEndAtAfterAndOwnerAndDataStatusAllIgnoreNull(
            ObjectId communityId, Integer chargeType, Integer type, Date endAt, String carNo, int dataStatus, ObjectId communityId1, Integer chargeType1,
            Integer type1, Date endAt1, String owner, int dataStatus1, Pageable pageable);

    Page<Identity> CommunityIdAndCarTypeAndTypeAndCarNoAndDataStatusOrCommunityIdAndCarTypeAndTypeAndOwnerAndDataStatusAllIgnoreNull(
            ObjectId communityId, Integer chargeType, Integer type, String carNo, int dataStatus, ObjectId communityId1, Integer chargeType1, Integer type1,
            String owner, int dataStatus1, Pageable pageable);

    Page<Identity>
    findByCommunityIdAndCarTypeAndTypeAndEndAtBeforeAndCarNoAndDataStatusOrCommunityIdAndCarTypeAndTypeAndEndAtBeforeAndOwnerAndDataStatusAllIgnoreNull(
            ObjectId communityId, Integer chargeType, Integer type, Date endAt, String carNo, int dataStatus, ObjectId communityId1, Integer chargeType1,
            Integer type1, Date endAt1, String owner, int dataStatus1, Pageable pageable);

    Identity updateByIdAndDataStatus(Identity identity, ObjectId id, Integer dataStatus);
}
