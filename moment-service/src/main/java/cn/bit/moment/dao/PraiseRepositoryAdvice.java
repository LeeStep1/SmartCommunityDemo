package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Praise;
import cn.bit.facade.vo.IncrementalRequest;
import org.bson.types.ObjectId;

import java.util.List;

public interface PraiseRepositoryAdvice {
    List<Praise> incrementalPraiseList(IncrementalRequest incrementalRequest);

    int updateMultiByMomentIdAndCreatorId(Praise toUpdate, ObjectId momentId, ObjectId creatorId);

    Integer insertPraiseWithChecked(Praise praise);

    List<Praise> incrementalMyPraiseList(IncrementalRequest incrementalRequest, ObjectId communityId, ObjectId uid);

    Long statisticsPraise(ObjectId communityId, ObjectId creatorId);
}
