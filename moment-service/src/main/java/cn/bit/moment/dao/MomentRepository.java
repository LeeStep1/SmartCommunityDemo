package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Moment;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface MomentRepository
        extends MomentRepositoryAdvice, MongoDao<Moment, ObjectId>, MongoRepository<Moment, ObjectId> {

    List<Moment> findByIdIn(Collection<ObjectId> momentIds);

    Page<Moment> findByIdIn(Collection<ObjectId> newShieldingMomentIds, Pageable pageable);

	Moment updateById(Moment toAudit, ObjectId id);

	Long countByCommunityIdAndCreatorIdAndStatusNotInAndDataStatus(ObjectId communityId, ObjectId creatorId,
                                                                   List<Integer> status, int dataStatus);

    Page<Moment> findByCommunityIdAndTypeAndStatusAndCreatorIdInAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
            ObjectId communityId, Integer type, Integer status, Collection<ObjectId> creatorIds,
            Date createStart, Date createEnd, int dataStatus, Pageable pageable);

    Page<Moment> findByCommunityIdAndStatusAndCreatorIdInAndReportNumGreaterThanEqualAndDataStatusAllIgnoreNull(
            ObjectId communityId, Integer status, Collection<ObjectId> creatorIds, Integer reportNum,
            int dataStatus, Pageable pageable);
}
