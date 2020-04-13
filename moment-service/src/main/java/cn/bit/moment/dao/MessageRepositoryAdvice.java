package cn.bit.moment.dao;

import cn.bit.facade.model.moment.Message;
import cn.bit.facade.vo.IncrementalRequest;
import org.bson.types.ObjectId;

import java.util.List;

public interface MessageRepositoryAdvice {

    List<Message> findByIncrementalRequestAndCommunityIdAndUserId(IncrementalRequest incrementalRequest,
                                                                  ObjectId communityId, ObjectId uid);
}
