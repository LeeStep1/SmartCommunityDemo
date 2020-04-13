package cn.bit.fees.dao;

import cn.bit.facade.model.fees.Bill;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface BillRepository extends MongoDao<Bill, ObjectId>, MongoRepository<Bill, ObjectId> {

    <T> Page<T> findByCommunityIdAndRoomIdAndProprietorIdAndStatusAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAllIgnoreNull(
            ObjectId communityId, ObjectId roomId, ObjectId proprietorId, Integer status, Date startAt, Date endAt, Pageable pageable, Class<T> tClass);

    Bill updateById(Bill toUpdate, ObjectId id);

    void deleteById(ObjectId id);

    int updateByIdInAndStatus(Bill toUpdate, Collection<ObjectId> ids, Integer billStatus);

    Bill updateByIdAndStatus(Bill toUpdate, ObjectId id, Integer billStatus);

    List<Bill> findByIdIn(List<ObjectId> ids);

    Bill updateByTradeIdAndStatus(Bill toUpdate, Long tradeId, Integer billStatus);

    Bill updateWithUnsetIfNullTradeIdByTradeIdAndStatus(Bill toUpdate, Long tradeId, Integer billStatus);

    List<Bill> findByCommunityIdAndBuildingIdInAndStatusAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAllIgnoreNullOrderByBuildingIdAscRoomIdAsc(
            ObjectId communityId, Set<ObjectId> buildingIds, Integer status, Date startTime, Date endTime);

    long countByCommunityIdAndStatus(ObjectId communityId, Integer status);

    List<Bill> findByRoomIdAndStatus(ObjectId roomId, Integer key);
}
