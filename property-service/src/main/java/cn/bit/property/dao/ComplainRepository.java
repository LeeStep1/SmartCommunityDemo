package cn.bit.property.dao;

import cn.bit.facade.model.property.Complain;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ComplainRepository extends MongoDao<Complain, ObjectId>, MongoRepository<Complain, ObjectId> {
    List<Complain> findByCommunityIdAndMessageSourceAndDataStatusOrderByCreateAtDesc(
    		ObjectId communityId, Integer messageSource, int dataStatus, Pageable pageable);

    Page<Complain> findByCommunityIdAndUserIdAndUserNameRegexAndMessageSourceAndStatusInAndHiddenNotAndInvalidAndAnonymityNotAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
            ObjectId communityId, ObjectId userId, String userName, Integer messageSource, Collection<Integer> status,
            Boolean notHidden, Boolean invalid, Boolean nonAnonymity, Date startAt, Date endAt, int dataStatus, Pageable pageable);

    Complain findByIdAndDataStatus(ObjectId id, int dataStatus);

    Complain updateById(Complain toUpdate, ObjectId id);
}
