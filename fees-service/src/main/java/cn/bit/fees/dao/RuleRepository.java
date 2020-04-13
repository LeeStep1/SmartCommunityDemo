package cn.bit.fees.dao;

import cn.bit.facade.model.fees.Rule;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface RuleRepository extends MongoDao<Rule, ObjectId>, MongoRepository<Rule, ObjectId> {

    List<Rule> findAllByFeeItemIdAndDataStatus(ObjectId id, Integer dataStatus);

    List<Rule> findByCommunityIdAndFeeItemIdInAndDataStatus(ObjectId communityId, Set<ObjectId> feeItemIds, Integer dataStatus);

    List<Rule> findByFeeItemIdAndBuildingIdInAndDataStatus(ObjectId feeItemId, Set<ObjectId> buildingIds, int dataStatus);

    Page<Rule> findByCommunityIdAndFeeItemIdIgnoreNullAndDataStatus(ObjectId communityId, ObjectId feeItemId, int dataStatus, Pageable pageable);

	Long updateByFeeItemId(Rule rule, ObjectId feeItemId);

	Rule updateById(Rule rule, ObjectId id);
}
