package cn.bit.vehicle.dao;

import cn.bit.facade.model.vehicle.InOut;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface InOutRepository extends MongoRepository<InOut, ObjectId> {

    List<InOut> findByCommunityIdAndEnterAtNotNullAndLeaveAtNullAndTypeAndDataStatusOrderByEnterAtDesc(
            ObjectId communityId, Integer type, Integer dataStatus);

    List<InOut> findByCarNoAndCommunityIdAndEnterAtAfterAndEnterAtBeforeAndDataStatusAllIgnoreNullOrderByEnterAtDesc(
            String carNo, ObjectId communityId, Date inOutDate, Date nextDay, Integer dataStatus);
    
    Page<InOut> findByCommunityIdAndCarNoAndEnterAtAfterAndEnterAtBeforeAndLeaveAtAfterAndLeaveAtBeforeAndDataStatusAllIgnoreNull(
            ObjectId communityId, String carNo, Date enterAtAfter, Date enterAtBefore, Date leaveAtAfter, Date leaveAtBefore,
            Integer dataStatus, Pageable pageable);

    Long countByCommunityIdAndEnterAtNotNullAndLeaveAtNullAndTypeAndDataStatus(ObjectId communityId, int inOutType, int dataStatus);

    Page<InOut> findByCommunityIdAndDataStatus(ObjectId communityId, int dataStatus, Pageable pageable);

    Page<InOut> findByCommunityIdAndInGateAndEnterAtAfterAndEnterAtBeforeAndCarNoAndDataStatusOrCommunityIdAndOutGateAndLeaveAtAfterAndLeaveAtBeforeAndCarNoAndDataStatusAllIgnoreNull(
            ObjectId communityId, String inGate, Date startAt, Date endAt, String carNo, int dataStatus,
            ObjectId communityId1, String outGate, Date startAt1, Date endAt1, String carNo1, int dataStatus1,
            Pageable pageable);

    List<InOut> findByCommunityIdAndInGateAndEnterAtAfterAndEnterAtBeforeAndCarNoAndDataStatusOrCommunityIdAndOutGateAndLeaveAtAfterAndLeaveAtBeforeAndCarNoAndDataStatusAllIgnoreNull(
            ObjectId communityId, String inGate, Date startAt, Date endAt, String carNo, int dataStatus,
            ObjectId communityId1, String outGate, Date startAt1, Date endAt1, String carNo1, int dataStatus1);
}
