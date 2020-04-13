package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Moment;
import cn.bit.facade.vo.IncrementalRequest;
import org.bson.types.ObjectId;

import java.util.List;

public interface MomentRepositoryAdvice  {

    Moment updateNumByIdAndFieldName(ObjectId momentId, String fieldName, int num);

    List<Moment> findByIncrementalRequestAndCommunityId(IncrementalRequest incrementalRequest, ObjectId communityId);

    List<Moment> findByIncrementalRequestAndCommunityIdAndCreatorId(IncrementalRequest incrementalRequest,
                                                                    ObjectId communityId, ObjectId currUserId);
}
