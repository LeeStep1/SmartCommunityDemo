package cn.bit.fees.dao;

import cn.bit.facade.model.fees.PropFeeItem;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface PropFeeItemRepository extends MongoDao<PropFeeItem, ObjectId>, MongoRepository<PropFeeItem, ObjectId> {

    PropFeeItem findByCommunityIdAndItemNameAndDataStatus(ObjectId communityId, String itemName, int dataStatus);

    List<PropFeeItem> findByCommunityIdInAndDataStatus(Set<ObjectId> communityIds, int dataStatus);

    Page<PropFeeItem> findByCommunityIdAndDataStatus(ObjectId communityId, int dataStatus, Pageable pageable);

	PropFeeItem updateById(PropFeeItem toUpdate, ObjectId id);

	PropFeeItem findByIdAndDataStatus(ObjectId id, int dataStatus);
}
