package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Praise;
import cn.bit.framework.data.mongodb.MongoDao;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PraiseRepository
        extends PraiseRepositoryAdvice, MongoDao<Praise, ObjectId>, MongoRepository<Praise, ObjectId> {

    Praise findByMomentIdAndCreatorIdAndDataStatus(ObjectId momentId, ObjectId creatorId, int dataStatus);

    List<Praise> findByCreatorIdAndCommunityIdAndDataStatus(ObjectId currUserId, ObjectId communityId, int dataStatus);
}
