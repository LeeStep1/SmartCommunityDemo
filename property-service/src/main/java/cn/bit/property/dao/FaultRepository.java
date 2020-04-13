package cn.bit.property.dao;

import cn.bit.facade.model.property.Fault;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

/**
 * Created by fxiao
 * on 2018/3/7
 */
public interface FaultRepository extends MongoDao<Fault, ObjectId>, MongoRepository<Fault, ObjectId> {

	Fault updateById(Fault toUpdate, ObjectId id);

	Long countByCommunityIdAndFaultStatusAndDataStatus(ObjectId communityId, int faultStatus, int dataStatus);

	Long countByCommunityIdAndFaultStatusAndFaultItemAndDataStatus(ObjectId communityId, int faultStatus,
																   Integer faultItem, int dataStatus);

	Long countByCommunityIdAndFaultStatusAndEvaluationGradeAndDataStatus(ObjectId communityId, int faultStatus,
																		 Integer evaluationGrade, int dataStatus);

	Long countByCommunityIdAndFaultStatusAndRepairIdAndDataStatus(ObjectId communityId, Integer faultStatus,
																  ObjectId repairId, int dataStatus);

	Long countByCommunityIdAndFinishTimeGreaterThanEqualAndFinishTimeLessThan(ObjectId communityId,
																			  Date beginTime, Date finishTime);

	Page<Fault> findByCommunityIdAndUserIdAndUserNameRegexAndRepairIdAndFaultStatusAndPlayTimeGreaterThanEqualAndPlayTimeLessThanAndHiddenNotAndDataStatusAllIgnoreNull(
			ObjectId communityId, ObjectId userId, String userName, ObjectId repairId, Integer faultStatus,
			Date startTime, Date endTime, Boolean hidden, int dataStatus, Pageable pageable);

    Fault findByIdAndDataStatus(ObjectId id, int dataStatus);
}
