package cn.bit.fees.dao;

import cn.bit.facade.model.fees.PropertyBill;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface PropertyBillRepository extends MongoDao<PropertyBill, ObjectId>,
		MongoRepository<PropertyBill, ObjectId> {
    int updateByIdInAndDataStatusAndBillStatus(PropertyBill toUpdate,
                                               Collection<ObjectId> billIds, int dataStatus, Integer billStatus);

    List<PropertyBill> findByCommunityIdAndBillStatusAndDataStatusAndBuildingIdIgnoreNull(
            ObjectId communityId, Integer billStatus, int dataStatus, ObjectId buildingId);

	Long countByBuildingIdAndBillStatusAndOverdueDateLessThanAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
			ObjectId buildingId, Integer billStatus, Date overdueDate, Date startAt, Date endAt, int dataStatus);

	List<PropertyBill> findByBuildingIdInAndBillStatusAndOverdueDateLessThanAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
			Collection<ObjectId> buildingIds, Integer billStatus, Date overdueDate,
			Date startAt, Date endAt, int dataStatus);

	PropertyBill updateById(PropertyBill toUpdate, ObjectId id);

	PropertyBill updateByTradeIdAndBillStatusAndDataStatus(PropertyBill propertyBill,
	                                                       Long tradeId, Integer billStatus, int dataStatus);

	Page<PropertyBill> findByCommunityIdAndBuildingIdAndProprietorIdAndRoomLocationContainsAndBillStatusInAndOverdueDateLessThanAndMakeAtGreaterThanEqualAndMakeAtLessThanEqualAndDataStatusAllIgnoreNull(
			ObjectId communityId, ObjectId buildingId, ObjectId proprietorId, String roomLocation,
			Collection<Integer> billStatus, Date overdueDate, Date startMakeAt, Date endMakeAt,
			int dataStatus, Pageable pageable);

	List<PropertyBill> findByCommunityIdAndBillStatusAndOverdueDateLessThanAndDataStatus(
			ObjectId communityId, Integer billStatus, Date overdueDate, int dataStatus);

	long countByCommunityIdAndBuildingIdIgnoreNullAndBillStatusAndDataStatus(
			ObjectId communityId, ObjectId buildingId, Integer billStatus, int dataStatus);

	long countByCommunityIdAndBuildingIdIgnoreNullAndBillStatusAndOverdueDateLessThanAndDataStatus(
			ObjectId communityId, ObjectId buildingId, Integer billStatus, Date overdueDate, int dataStatus);
}
